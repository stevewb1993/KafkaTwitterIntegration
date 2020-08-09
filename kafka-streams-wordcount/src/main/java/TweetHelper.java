import com.google.gson.JsonParser;
import org.apache.kafka.streams.kstream.KStream;

public class TweetHelper {

    private static JsonParser jsonParser = new JsonParser();


    public static Integer WordCount (String tweet) {
        return tweet.split("\\s+").length; //this splits the tweet into
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
