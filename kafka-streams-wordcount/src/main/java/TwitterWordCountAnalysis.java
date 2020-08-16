import com.google.gson.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import TweetHelper.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TwitterWordCountAnalysis {

    private static JsonParser jsonParser = new JsonParser();

    public Topology createTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // input topic
        KStream<String, String> tweetStream = streamsBuilder.stream("tweets");

        tweetStream
                //parse the API tweet response as a Tweet Object
                .mapValues(tweetString -> new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject(), Tweet.class))
                // 3 - flatmap values split by space
                .flatMapValues(tweet -> tweetWordDateMapper(tweet))
                // 4 - select key to apply a key (we discard the old key)
                .selectKey((key, wordDate) -> wordDate.toString())
                // 5 - group by key before aggregation
                .groupByKey()
                // 6 - count occurences
                .count();

        tweetStream.print(Printed.toSysOut());


        //tweetStream.to("twitter-word-counts",Produced.with(Serdes.String(), Serdes.Long()));
        tweetStream.to("twitter-word-count");
        //System.out.println("test");

        return streamsBuilder.build();
    }

    public static void main(String[] args) {
        // create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "WordCounter");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

        TwitterWordCountAnalysis twitterWordCountAnalyser = new TwitterWordCountAnalysis();

        KafkaStreams streams = new KafkaStreams(twitterWordCountAnalyser.createTopology(), properties);

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