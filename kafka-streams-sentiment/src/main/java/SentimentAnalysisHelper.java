import TweetHelper.Tweet;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.codecommit.model.ActorDoesNotExistException;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SentimentAnalysisHelper {

    private static final JsonParser jsonParser = new JsonParser();
    private static AWSCredentialsProvider awsCreds;
    private static AmazonComprehend comprehendClient;

    public SentimentAnalysisHelper(String region, AWSCredentialsProvider awsCreds) {
        this.awsCreds = awsCreds;
        this.comprehendClient =
                AmazonComprehendClientBuilder.standard()
                        .withCredentials(awsCreds)
                        .withRegion(region)
                        .build();
    }

    //call to AWS Comprehend service to get sentiment
    //format of output https://docs.aws.amazon.com/comprehend/latest/dg/how-sentiment.html
    public DetectSentimentResult getSentimentAnalysis(String tweetText) {
        System.out.println("analysing tweet: " + tweetText);
        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(tweetText).withLanguageCode("en");
        return comprehendClient.detectSentiment(detectSentimentRequest);
    }

    //helper function to format the key date and sentiment data into flat json format that can be parsed by kafka connect jdbc sink
    //options are included for whether to include the detail of the sentiment analysis result as well as the tweet text
    public static JsonObject formatTweetWithSentiment(Tweet tweet, DetectSentimentResult tweetSentiment, List<String> propertiesList) {

        System.out.println(tweet.id);
        //validate requested data
        HashSet<String> validProperties = new HashSet<>(Arrays.asList(
                "sentimentDetail"
                , "overallSentiment"
                , "tweetText"
                , "tweetID"
                , "followers"
                , "userID"
                , "date"));

        if (!(validProperties.containsAll(propertiesList))) {
            throw new IllegalArgumentException("list of properties not valid");
        }
        //create new json to add the requested details into
        JsonObject sentimentAndTweetDetails = new JsonObject();
        //add sentiment detail
        if(propertiesList.contains("sentimentDetail")) {
            sentimentAndTweetDetails = jsonParser.parse(tweetSentiment.getSentimentScore().toString()).getAsJsonObject();
        }
        //add overall sentiment result
        if(propertiesList.contains("overallSentiment")) {
            sentimentAndTweetDetails.addProperty("overallSentiment", tweetSentiment.getSentiment());
        }
        //add tweet text
        if(propertiesList.contains("tweetText")) {
            sentimentAndTweetDetails.addProperty("tweetText", tweet.tweetText);
        }
        //add tweet id
        if(propertiesList.contains("tweetID")) {
            sentimentAndTweetDetails.addProperty("tweetID", tweet.id);
        }
        //add number of followers
        if(propertiesList.contains("followers")) {
            sentimentAndTweetDetails.addProperty("userFollowers", tweet.user.followersCount);
        }
        //add user id
        if(propertiesList.contains("userID")) {
            sentimentAndTweetDetails.addProperty("userID", tweet.user.id);
        };
        //add the date
        if(propertiesList.contains("date")) {
            //we  need to get the date of the tweet at the hour level so we can average the sentiment
            String tweetDate;
            SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH");
            try {
                tweetDate = tweet.formattedDate(shortDateFormatter);
            } catch (ParseException e) {
                tweetDate = "unknown date";
            }
            sentimentAndTweetDetails.addProperty("date", tweetDate);
        };
        return sentimentAndTweetDetails;
    }

    public static JsonObject formatTweetWithSentiment(Tweet tweet, DetectSentimentResult tweetSentiment) {
        List<String> propertiesList = Arrays.asList(
                "sentimentDetail"
                , "overallSentiment"
                , "tweetText"
                , "tweetID"
                , "followers"
                , "userID"
                , "date");


        return formatTweetWithSentiment(tweet, tweetSentiment, propertiesList);
    }

    public DetectEntitiesResult detectEntities(String tweetText) {
        DetectEntitiesRequest detectEntitiesRequest = new DetectEntitiesRequest().withText(tweetText)
                .withLanguageCode("en");
        DetectEntitiesResult detectEntitiesResult  = comprehendClient.detectEntities(detectEntitiesRequest);
        return detectEntitiesResult;
    }

    public List<JsonObject> addEntitiesToSentimentResult (Tweet tweet, DetectSentimentResult sentimentResult) {

        DetectEntitiesResult entities = detectEntities(tweet.tweetText);
        JsonObject tweetWithSentiment = formatTweetWithSentiment(tweet, sentimentResult);

        List<JsonObject> entitiesWithSentiment = new ArrayList<JsonObject>();
        for (Entity entity: entities.getEntities()) {
            JsonObject singleEntityWithTweetSentiment = tweetWithSentiment;
            singleEntityWithTweetSentiment.addProperty("Entity", entity.getText());
            singleEntityWithTweetSentiment.addProperty("Score", entity.getScore());
            singleEntityWithTweetSentiment.addProperty("Type", entity.getType());
            entitiesWithSentiment.add(singleEntityWithTweetSentiment);
        }
        return entitiesWithSentiment;

    }
}
