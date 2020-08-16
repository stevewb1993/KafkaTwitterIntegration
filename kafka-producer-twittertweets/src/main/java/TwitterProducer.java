import com.google.common.collect.Lists;
import com.google.gson.JsonParser;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
    List<String> terms = Lists.newArrayList("coronavirus");
    private static JsonParser jsonParser = new JsonParser();

    public TwitterProducer(){}

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run(){
        //create twitter client

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

        Client client = createTwitterClient(msgQueue, terms);

        // Attempts to establish a connection.
        client.connect();

        //create kafka producer
        KafkaProducer<String,String> producer = createKafkaProducer();

        String topic = "test-topic";
        String key = null;

        //add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application");
            logger.info("stopping client");
            client.stop();
            logger.info("stopping producer");
            producer.close();
            logger.info("stopped successfully");
        }));


        //loop to send tweets to kafka
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
                System.out.println(msg);
            }
            catch(Exception e) {
                System.out.println("something went wrong");
                System.out.println(e);
                e.printStackTrace();
                client.stop();
            }
            if(msg != null) {
                ProducerRecord<String, String > producerRecord = new ProducerRecord<String, String>(topic, key, msg);
                int followers = extractUserFollowersInTweet(producerRecord.value());
                System.out.println(followers);
                if(followers > 100) {
                    System.out.println("message is sent");
                    producer.send(producerRecord, new Callback() {
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            if(exception == null) {
                                //the record was successfully sent
                                logger.info("Received new metadata. \n +" +
                                        "Topic: " + metadata.topic() + "\n" +
                                        "Partition:: " +metadata.partition() + "\n" +
                                        "Timestamp" + metadata.timestamp() + "\n" +
                                        "Offset" + metadata.offset());
                            }
                            else {
                                logger.error("error while producing " + exception);
                            }
                        }
                    });
                }
            }
        }
        logger.info("End of Application");


    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue, List<String> terms){

        //Properties properties = new Properties();
        //try {
        //    String currentDirectory = System.getProperty("user.dir");
        //    System.out.println("The current working directory is " + currentDirectory);
        //    FileInputStream propertiesFile = new FileInputStream("kafka-producer-twittertweets\\src\\main\\java\\app.properties");
        //    properties.load(propertiesFile);
        //}
        //catch (Exception e) {
        //    System.out.println("Error: couldn't load properties file");
        //    System.out.println(e);
        //    System.exit(1);
        //}

        //properties.setProperty("apiKey", System.getenv("apiKey"));
        //properties.setProperty("apiKey", System.getenv("apiKey"));


        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms

        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(System.getenv("apiKey"), System.getenv("apiSecret"), System.getenv("bearerToken"), System.getenv("tokenSecret"));

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();

        return hosebirdClient;


    }

    public KafkaProducer<String,String>  createKafkaProducer () {
        //prod properties
        Properties properties = new Properties();

        String bootstrapServers = "35.178.180.144:9092";
        String acksSetting = "1";

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG,acksSetting);
        properties.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,"60000"); //1 min timeout

        //properties to improve throughput (at cost of some latency)
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG,"5000");
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024)); //32kb

        //producer

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        return producer;

    }


    public static Integer extractUserFollowersInTweet(String tweetJson){
        // gson library
        try {
            return jsonParser.parse(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();
        }
        catch (NullPointerException e){
            return 0;
        }
    }


}
