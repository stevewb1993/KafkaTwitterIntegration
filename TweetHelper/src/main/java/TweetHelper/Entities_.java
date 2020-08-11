
package TweetHelper;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entities_ implements Serializable
{

    @SerializedName("hashtags")
    @Expose
    public List<Object> hashtags = null;
    @SerializedName("urls")
    @Expose
    public List<Url_> urls = null;
    @SerializedName("user_mentions")
    @Expose
    public List<UserMention> userMentions = null;
    @SerializedName("symbols")
    @Expose
    public List<Object> symbols = null;
    private final static long serialVersionUID = 2018872542550949063L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Entities_() {
    }

    /**
     * 
     * @param urls
     * @param hashtags
     * @param userMentions
     * @param symbols
     */
    public Entities_(List<Object> hashtags, List<Url_> urls, List<UserMention> userMentions, List<Object> symbols) {
        super();
        this.hashtags = hashtags;
        this.urls = urls;
        this.userMentions = userMentions;
        this.symbols = symbols;
    }

}
