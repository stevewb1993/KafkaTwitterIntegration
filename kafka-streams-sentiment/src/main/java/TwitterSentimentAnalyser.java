import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class TwitterSentimentAnalyser {

    private final JsonParser jsonParser = new JsonParser();


    public Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();


        KStream<String, String> textLines = builder.stream("raw-twitter");

        return builder.build();
    }

    public static void main(String[] args) {

        //String currentDirectory = System.getProperty("user.dir");
        //System.out.println("The current working directory is " + currentDirectory);

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-word-counter");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "35.178.180.144:9092");
        //config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        TwitterSentimentAnalyser wordCountApp = new TwitterSentimentAnalyser();

        KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}
