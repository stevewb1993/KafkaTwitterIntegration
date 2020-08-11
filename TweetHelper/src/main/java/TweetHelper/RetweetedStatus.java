
package TweetHelper;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RetweetedStatus implements Serializable
{

    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("text")
    @Expose
    public String text;
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
    public TweetHelper.User_ user;
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
    public TweetHelper.Entities entities;
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
    private final static long serialVersionUID = -4852691623507544664L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RetweetedStatus() {
    }

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
     */
    public RetweetedStatus(String createdAt, Integer id, String idStr, String text, String source, Boolean truncated, Object inReplyToStatusId, Object inReplyToStatusIdStr, Object inReplyToUserId, Object inReplyToUserIdStr, Object inReplyToScreenName, TweetHelper.User_ user, Object geo, Object coordinates, Object place, Object contributors, Boolean isQuoteStatus, Integer quoteCount, Integer replyCount, Integer retweetCount, Integer favoriteCount, TweetHelper.Entities entities, Boolean favorited, Boolean retweeted, Boolean possiblySensitive, String filterLevel, String lang) {
        super();
        this.createdAt = createdAt;
        this.id = id;
        this.idStr = idStr;
        this.text = text;
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
    }

}
