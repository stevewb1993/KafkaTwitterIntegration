import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;

import TweetHelper.Tweet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

public class TwitterWordCounter {

    private static JsonParser jsonParser = new JsonParser();

    public Topology createTopology(){
        StreamsBuilder builder = new StreamsBuilder();
        // 1 - stream from Kafka

        KStream<String, String> textLines = builder.stream("test-topic");
        KTable<String, Long> wordCounts = textLines
                // 2 - map values to lowercase
                .mapValues(tweetString -> new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject(), Tweet.class))
                // can be alternatively written as:
                // .mapValues(String::toLowerCase)
                // 3 - flatmap values split by space
                .flatMapValues(tweet -> tweetWordDateMapper(tweet))
                // 4 - select key to apply a key (we discard the old key)
                .selectKey((key, word) -> word)
                // 5 - group by key before aggregation
                .groupByKey()
                // 6 - count occurences
                .count(Materialized.as("Counts"));

        // 7 - to in order to write the results back to kafka
        wordCounts.toStream().to("test-output", Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application10");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "35.178.180.144:9092");
        //config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        TwitterWordCounter wordCountApp = new TwitterWordCounter();

        KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        // Update:
        // print the topology every 10 seconds for learning purposes
        //while(true){
        //    streams.localThreadsMetadata().forEach(data -> System.out.println(data));
        //    try {
        //        Thread.sleep(5000);
        //    } catch (InterruptedException e) {
        //        break;
        //    }
        //}


    }

    public static List<String> tweetWordDateMapper(Tweet tweet) {
        try{

            List<String> words = Arrays.asList(tweet.tweetText.split("\\W+"));
            List<String> tweetsJson = new ArrayList<String>();
            for(String word: words) {
                JsonObject tweetJson = new JsonObject();
                tweetJson.add("date", new JsonPrimitive(tweet.formattedDate().toString()));
                tweetJson.add("word", new JsonPrimitive(word));
                tweetsJson.add(tweetJson.toString());
            }

            return tweetsJson;
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println(tweet.serialize().toString());
            return new ArrayList<String>();
        }

    }
}