import java.util.*;

import TweetHelper.Tweet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.internal.bind.util.ISO8601Utils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

public class TwitterWordCounter {

    //Create serdes for json
    Map<String, Object> serdeProps = new HashMap<>();


    private final JsonParser jsonParser = new JsonParser();



    public Topology createTopology(){
        StreamsBuilder builder = new StreamsBuilder();


        KStream<String, String> textLines = builder.stream("test-topic2");
        KTable<String, Long> wordCounts = textLines
                //parse each tweet as a tweet object
                .mapValues(tweetString -> new Gson().fromJson(jsonParser.parse(tweetString).getAsJsonObject(), Tweet.class))
                //map each tweet object to a list of json objects, each of which containing a word from the tweet and the date of the tweet
                .flatMapValues(TwitterWordCounter::tweetWordDateMapper)
                .mapValues(x -> x.toString())
                //update the key so it matches the word-date combination so we can do a groupBy and count instances
                .selectKey((key, wordDate) -> wordDate.toString())
                .groupByKey()
                .count(Materialized.as("Counts"));
                //.mapValues(value -> value.toString());

        /*
            In order to structure the data so that it can be ingested into SQL, the value of each item in the stream must be straightforward: property, value
            so we have to:
             1. take the columns which include the dimensional data and put this into the value of the stream.
             2. lable the count with 'count' as the column name
         */
        KStream<String, String> wordCountsStructured = wordCounts.toStream()
                .map((key, value) -> new KeyValue<>(null, MapValuesToIncludeColumnData(key, value))); //key.add("count", new JsonPrimitive(value))));

        KStream<String, String> wordCountsPeek = wordCountsStructured.peek(
                (key, value) -> System.out.println("key: " + key + " value:" + value)
        );

        wordCountsStructured.to("test-output2", Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application1111");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "35.178.180.144:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        TwitterWordCounter wordCountApp = new TwitterWordCounter();

        KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

    //this method is used for taking a tweet and transforming it to a representation of the words in it plus the date
    public static List<JsonObject> tweetWordDateMapper(Tweet tweet) {
        try{

            List<String> words = Arrays.asList(tweet.tweetText.toLowerCase().split("\\W+"));
            List<JsonObject> tweetsJson = new ArrayList<JsonObject>();
            for(String word: words) {
                JsonObject tweetJson = new JsonObject();
                tweetJson.add("date", new JsonPrimitive(tweet.formattedDate().toString()));
                tweetJson.add("word", new JsonPrimitive(word));
                tweetsJson.add(tweetJson);
            }

            return tweetsJson;
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println(tweet.serialize().toString());
            return new ArrayList<JsonObject>();
        }

    }

    public String MapValuesToIncludeColumnData(String key, Long countOfWord) {
        JsonObject jkey = jsonParser.parse(key).getAsJsonObject();
        jkey.addProperty("count", countOfWord); //new JsonPrimitive(count));
        return jkey.toString();
    }
    


//    public class JsonSerializer implements Serializer<JsonNode> {
//        private final ObjectMapper objectMapper = new ObjectMapper();
//
//        public JsonSerializer() {
//            //Nothing to do
//        }
//
//        @Override
//        public void configure(Map<String, ?> config, boolean isKey) {
//            //Nothing to Configure
//        }
//
//        @Override
//        public byte[] serialize(String topic, JsonNode data) {
//            if (data == null) {
//                return null;
//            }
//            try {
//                return objectMapper.writeValueAsBytes(data);
//            } catch (JsonProcessingException e) {
//                throw new SerializationException("Error serializing JSON message", e);
//            }
//        }
//
//        @Override
//        public void close() {
//
//        }
//    }

}