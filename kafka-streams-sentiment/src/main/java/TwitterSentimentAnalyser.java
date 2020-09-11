import TweetHelper.Tweet;
import com.amazonaws.services.comprehend.model.SentimentScore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.util.Pair;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;

public class TwitterSentimentAnalyser {

    private final JsonParser jsonParser = new JsonParser();


    //create credentials and client for sentiment analysis
    private static final AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();
    private static final AmazonComprehend comprehendClient =
            AmazonComprehendClientBuilder.standard()
                    .withCredentials(awsCreds)
                    .withRegion("eu-west-2")
                    .build();

    public Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> rawTwitterStream = builder.stream("raw-twitter");
        KStream<String, KeyValue<Tweet, DetectSentimentResult>> twitterSentiment = rawTwitterStream
                //parse each message as a tweet object
                .mapValues(tweetString -> new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject(), Tweet.class))
                //retrieve sentiment score from AWS comprehend client.
                //return key value of tweet and sentiment so additional analysis / comparisons can be made between the tweet info and the sentiment results
                .mapValues(tweet -> new KeyValue<Tweet, DetectSentimentResult>(tweet, getSentimentAnalysis(tweet.tweetText)));

        KTable<String, Long> twitterSentimentCount = twitterSentiment
                //retrieve date and final sentiment score of tweet and parse as a flat json format
                .mapValues(tweetDetails -> formatTweetWithSentiment(tweetDetails.key,tweetDetails.value))
                //convert to string to avoid issues with Serdes (fix this later)
                .mapValues(JsonElement::toString)
                //change the key to facilitate group by
                .selectKey((key,value) -> value)
                .groupByKey()
                .count(Materialized.as("Counts"));

        KStream<String, String> twitterSentimentCountFormatted = twitterSentimentCount.toStream()
                //include date and sentiment info in the value
                //add the schema to the message
                .map((key, value) -> new KeyValue<>(null, addSchemaToKafkaPayload(MapValuesToIncludeColumnData(key, value))));

        KStream<String, String> wordCountsPeek = twitterSentimentCountFormatted.peek(
                (key, value) -> System.out.println("key: " + key + " value:" + value)
        );

        twitterSentimentCountFormatted.to("twittersentiment");
        return builder.build();
    }

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-sentiment");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "35.178.180.144:9092");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        TwitterSentimentAnalyser twitterSentimentAnalyser = new TwitterSentimentAnalyser();

        KafkaStreams streams = new KafkaStreams(twitterSentimentAnalyser.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }


    //call to AWS Comprehend service to get sentiment
    //format of output https://docs.aws.amazon.com/comprehend/latest/dg/how-sentiment.html
    public DetectSentimentResult getSentimentAnalysis (String tweetText) {
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(tweetText).withLanguageCode("en");
        return comprehendClient.detectSentiment(detectSentimentRequest);
    }

    //helper function to format the key date and sentiment data into flat json format that can be parsed by kafka connect jdbc sink
    public JsonObject formatTweetWithSentiment (Tweet tweet, DetectSentimentResult tweetSentiment) {

        //we  need to get the date of the tweet at the hour level so we can average the sentiment
        String tweetDate = null;
        SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH");
        try {
            tweetDate = tweet.formattedDate(shortDateFormatter);
        } catch (ParseException e) {
            tweetDate = "unknown date";
        }

        //empty json to put the results into
        JsonObject tweetSentimentResultWithDate = new JsonObject();
        //add the datetime at hour level to facilitate aggregation
        tweetSentimentResultWithDate.addProperty("sentiment", tweetSentiment.getSentiment());
        //add the sentiment (positive, neutral, mixed, negative)
        tweetSentimentResultWithDate.addProperty("date", tweetDate);
        return tweetSentimentResultWithDate;

    }

    //after aggregation of the stream, the value of the msg only includes the count.
    //this helper function adds back in the sentiment and date (format required for jdbc sink)
    public String MapValuesToIncludeColumnData(String key, Long countOfWord) {
        JsonObject jkey = jsonParser.parse(key).getAsJsonObject();
        jkey.addProperty("count", countOfWord); //new JsonPrimitive(count));
        return jkey.toString();
    }

    //add schema to the payload so messages can be read by kafka connect
    public String addSchemaToKafkaPayload(String payload) {
        JsonObject fullMessage = new JsonObject();

        //add payload
        fullMessage.add("payload", jsonParser.parse(payload).getAsJsonObject());
        //add schema
        String schema =
                "{\"type\": \"struct\"," +
                        " \"optional\": false," +
                        " \"version\": 1," +
                        " \"fields\": [" +
                        "{ \"field\": \"date\", \"type\": \"string\", \"optional\": true }, " +
                        "{ \"field\": \"sentiment\", \"type\": \"string\", \"optional\": true }, " +
                        "{ \"field\": \"count\", \"type\": \"int64\", \"optional\": true }]}";

        fullMessage.add("schema", jsonParser.parse(schema).getAsJsonObject());

        return fullMessage.toString();
    }

}
