
package TweetHelper;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Tweet implements Serializable
{

    public int wordCount() {
        return this.tweetText.split("\\s+").length;
    }

    public String formattedDate(SimpleDateFormat requiredFormat) throws ParseException {

        String tweetAPIDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; //"E MMMM dd HH:mm:ss zzz yyyy";

        SimpleDateFormat  twitterAPIDateFormatter = new SimpleDateFormat(tweetAPIDateFormat, Locale.ENGLISH);
        try {
            System.out.println("tweet date: " + this.createdAt);
            Date parsedDate = twitterAPIDateFormatter.parse(this.createdAt);
            System.out.println("parsed date of tweet: " + parsedDate);
            return requiredFormat.format(parsedDate);
        } catch (ParseException e) {
            //gets the current date in the correct format if we can't get it from the tweet
            System.out.println("Couldn't extract date so assuming current timestamp of application");
            return requiredFormat.format(new Date(System.currentTimeMillis()));
        }
    }


    public JsonObject serialize() {
        Gson gson = new Gson();
        String tweetString = gson.toJson(this);
        return new JsonParser().parse(tweetString).getAsJsonObject();
    }

    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("id")
    @Expose
    public Long id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("text")
    @Expose
    public String tweetText;
    @SerializedName("source")
    @Expose
    public String source;
    @SerializedName("truncated")
    @Expose
    public Boolean truncated;
    @SerializedName("in_reply_to_status_id")
    @Expose
    public Object inReplyToStatusId;
    @SerializedName("in_reply_to_status_id_str")
    @Expose
    public Object inReplyToStatusIdStr;
    @SerializedName("in_reply_to_user_id")
    @Expose
    public Object inReplyToUserId;
    @SerializedName("in_reply_to_user_id_str")
    @Expose
    public Object inReplyToUserIdStr;
    @SerializedName("in_reply_to_screen_name")
    @Expose
    public Object inReplyToScreenName;
    @SerializedName("user")
    @Expose
    public User user;
    @SerializedName("geo")
    @Expose
    public Object geo;
    @SerializedName("coordinates")
    @Expose
    public Object coordinates;
    @SerializedName("place")
    @Expose
    public Object place;
    @SerializedName("contributors")
    @Expose
    public Object contributors;
    @SerializedName("retweeted_status")
    @Expose
    public RetweetedStatus retweetedStatus;
    @SerializedName("is_quote_status")
    @Expose
    public Boolean isQuoteStatus;
    @SerializedName("quote_count")
    @Expose
    public Integer quoteCount;
    @SerializedName("reply_count")
    @Expose
    public Integer replyCount;
    @SerializedName("retweet_count")
    @Expose
    public Integer retweetCount;
    @SerializedName("favorite_count")
    @Expose
    public Integer favoriteCount;
    @SerializedName("entities")
    @Expose
    public Entities entities;
    @SerializedName("favorited")
    @Expose
    public Boolean favorited;
    @SerializedName("retweeted")
    @Expose
    public Boolean retweeted;
    @SerializedName("possibly_sensitive")
    @Expose
    public Boolean possiblySensitive;
    @SerializedName("filter_level")
    @Expose
    public String filterLevel;
    @SerializedName("lang")
    @Expose
    public String lang;
    @SerializedName("timestamp_ms")
    @Expose
    public String timestampMs;
    private final static long serialVersionUID = -9041467423457404475L;

    /**
     * No args constructor for use in serialization
     *
     */

    /**
     *
     * @param inReplyToUserId
     * @param source
     * @param filterLevel
     * @param retweeted
     * @param geo
     * @param createdAt
     * @param inReplyToStatusId
     * @param quoteCount
     * @param retweetedStatus
     * @param inReplyToStatusIdStr
     * @param id
     * @param text
     * @param place
     * @param lang
     * @param favorited
     * @param idStr
     * @param coordinates
     * @param truncated
     * @param isQuoteStatus
     * @param inReplyToScreenName
     * @param replyCount
     * @param possiblySensitive
     * @param entities
     * @param contributors
     * @param inReplyToUserIdStr
     * @param user
     * @param retweetCount
     * @param favoriteCount
     * @param timestampMs
     */
    public Tweet(String createdAt, Long id, String idStr, String text, String source, Boolean truncated, Object inReplyToStatusId, Object inReplyToStatusIdStr, Object inReplyToUserId, Object inReplyToUserIdStr, Object inReplyToScreenName, User user, Object geo, Object coordinates, Object place, Object contributors, RetweetedStatus retweetedStatus, Boolean isQuoteStatus, Integer quoteCount, Integer replyCount, Integer retweetCount, Integer favoriteCount, Entities entities, Boolean favorited, Boolean retweeted, Boolean possiblySensitive, String filterLevel, String lang, String timestampMs) {
        super();
        this.createdAt = createdAt;
        this.id = id;
        this.idStr = idStr;
        this.tweetText = text;
        this.source = source;
        this.truncated = truncated;
        this.inReplyToStatusId = inReplyToStatusId;
        this.inReplyToStatusIdStr = inReplyToStatusIdStr;
        this.inReplyToUserId = inReplyToUserId;
        this.inReplyToUserIdStr = inReplyToUserIdStr;
        this.inReplyToScreenName = inReplyToScreenName;
        this.user = user;
        this.geo = geo;
        this.coordinates = coordinates;
        this.place = place;
        this.contributors = contributors;
        this.retweetedStatus = retweetedStatus;
        this.isQuoteStatus = isQuoteStatus;
        this.quoteCount = quoteCount;
        this.replyCount = replyCount;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.entities = entities;
        this.favorited = favorited;
        this.retweeted = retweeted;
        this.possiblySensitive = possiblySensitive;
        this.filterLevel = filterLevel;
        this.lang = lang;
        this.timestampMs = timestampMs;
    }




}
