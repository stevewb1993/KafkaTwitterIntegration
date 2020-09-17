import TweetHelper.Tweet;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TwitterSentimentStream {

    private final JsonParser jsonParser = new JsonParser();

    //settings for AWS Comprehend connection
    private static final String region = "eu-west-2";
    private static final AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
    private static final SentimentAnalysisHelper sentimentAnalysisHelper = new SentimentAnalysisHelper(region, awsCreds);

    //ratio of tweets to analyse. because AWS comprehend is expensive.
    private static final int sample = 10;
    private static final int minimumFollowers = 1000;

    //schemas for output topics
    private static final String twitterSentimentCountsSchema =
            "{\"type\": \"struct\"," +
                    " \"optional\": false," +
                    " \"version\": 1," +
                    " \"fields\": [" +
                    "{ \"field\": \"date\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"overallSentiment\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"count\", \"type\": \"int64\", \"optional\": true }]}";

    private static final String twitterSentimentDetailSchema =
            "{\"type\": \"struct\"," +
                    " \"optional\": false," +
                    " \"version\": 1," +
                    " \"fields\": [" +
                    "{ \"field\": \"Positive\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Negative\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Neutral\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Mixed\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"overallSentiment\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"tweetText\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"tweetID\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"userFollowers\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"userID\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"date\", \"type\": \"string\", \"optional\": true }]}";

    private static final String twitterSentimentWithEntitiesSchema =
            "{\"type\": \"struct\"," +
                    " \"optional\": false," +
                    " \"version\": 1," +
                    " \"fields\": [" +
                    "{ \"field\": \"Positive\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Negative\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Neutral\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"Mixed\", \"type\": \"double\", \"optional\": true }, " +
                    "{ \"field\": \"overallSentiment\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"tweetText\", \"type\": \"string\", \"optional\": true }, " +
                    "{ \"field\": \"tweetID\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"userFollowers\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"userID\", \"type\": \"int64\", \"optional\": true }, " +
                    "{ \"field\": \"date\", \"type\": \"string\", \"optional\": true }," +
                    "{ \"field\": \"Entity\", \"type\": \"string\", \"optional\": true }," +
                    "{ \"field\": \"Score\", \"type\": \"double\", \"optional\": true }," +
                    "{ \"field\": \"Type\", \"type\": \"string\", \"optional\": true }]}";

    public Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> rawTweets = builder.stream("tweets");

        //consume the tweets topic and perform sentiment analysis
        // note: filtered for a random sample of tweets posted by users with large number of followers (for cost purposes)
        KStream<String, KeyValue<Tweet, DetectSentimentResult>> twitterSentiment = rawTweets
                .mapValues(tweetString -> new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject().get("payload"), Tweet.class))
                .filter((k,tweet) -> tweet.user.followersCount > minimumFollowers)
                .filter((k,tweet) -> (int) (Math.random() * sample) == 1)
                //retrieve sentiment score from AWS comprehend client.
                //return key value of tweet and sentiment so additional analysis / comparisons can be made between the tweet info and the sentiment results
                .mapValues(tweet -> new KeyValue<>(tweet, sentimentAnalysisHelper.getSentimentAnalysis(tweet.tweetText)));


        ///////////
        //SINK 1: Format the output with all details of the sentiment analysis for full drill down
        ///////////
        twitterSentiment
                //parse the tweet and ALL sentiment details into a flat json as required for Kafka Connect JDBC sink
                //format date to ISO8601 standard
                .mapValues(tweetDetails -> sentimentAnalysisHelper.formatTweetWithSentiment(tweetDetails.key,tweetDetails.value, new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss'Z'")))
                //add schema
                .mapValues(sentimentResults -> addSchemaToKafkaPayload(sentimentResults,twitterSentimentDetailSchema))
                .to("twittersentimentdetail");



        ///////////
        //SINK 2: Extract the entities from the text alongside the previous sentiment scores
        ///////////
        twitterSentiment
                //parse the tweet and ALL sentiment and entity details into a flat json as required for Kafka Connect JDBC sink
                .flatMapValues(tweetDetails -> sentimentAnalysisHelper.addEntitiesToSentimentResult(tweetDetails.key,tweetDetails.value, new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss'Z'")))
                //add schema
                .mapValues(sentimentResults -> addSchemaToKafkaPayload(sentimentResults,twitterSentimentWithEntitiesSchema))
                .to("TwitterSentimentWithEntities");

        ///////////
        //SINK 3: extract the overall sentiment score and perform a count for analysis
        ///////////
        //perform aggregation count of the overall sentiment results (positive, negative, neutral, mixed), grouped by date and hour
        List<String> requiredProperties = Arrays.asList("overallSentiment", "date");
        KTable<String, Long> twitterSentimentCount = twitterSentiment
                //parse the tweet and required sentiment details into a flat json as required for Kafka Connect JDBC sink
                //format date to ISO8601 standard
                .mapValues(tweetDetails -> sentimentAnalysisHelper.formatTweetWithSentiment(tweetDetails.key,tweetDetails.value, requiredProperties,new SimpleDateFormat("yyyy-MM-dd HH")))
                //convert to string to avoid issues with Serdes (fix this later)
                .mapValues(JsonElement::toString)
                //change the key to facilitate group by
                .selectKey((key,value) -> value)
                .groupByKey()
                .count(Materialized.as("Counts"));

        //add formatting and send to twittersentiment topic
        twitterSentimentCount.toStream()
                //include date and sentiment info in the value
                //add the schema to the message
                .map((key, value) ->
                        new KeyValue<>(null,
                                addSchemaToKafkaPayload(
                                        MapValuesToIncludeColumnData(key, value)
                                        , twitterSentimentCountsSchema)))
                .to("twittersentiment");
        return builder.build();
    }

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafkasentiment");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "35.178.180.144:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        TwitterSentimentStream twitterSentimentStream = new TwitterSentimentStream();

        KafkaStreams streams = new KafkaStreams(twitterSentimentStream.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    //after aggregation of the stream, the value of the msg only includes the count.
    //this helper function adds back in the sentiment and date (format required for jdbc sink)
    public JsonObject MapValuesToIncludeColumnData(String key, Long countOfWord) {
        JsonObject jkey = jsonParser.parse(key).getAsJsonObject();
        jkey.addProperty("count", countOfWord); //new JsonPrimitive(count));
        return jkey;
    }

    //add schema to the payload so messages can be read by kafka connect
    public String addSchemaToKafkaPayload(JsonObject payload, String schema) {
        JsonObject fullMessage = new JsonObject();
        //add payload
        fullMessage.add("payload", payload);
        //add schema
        fullMessage.add("schema", jsonParser.parse(schema).getAsJsonObject());
        return fullMessage.toString();
    }
}