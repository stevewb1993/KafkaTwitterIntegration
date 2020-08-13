
package TweetHelper;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Entities implements Serializable
{

    @SerializedName("hashtags")
    @Expose
    public List<Object> hashtags = null;
    @SerializedName("urls")
    @Expose
    public List<TweetHelper.Url> urls = null;
    @SerializedName("user_mentions")
    @Expose
    public List<Object> userMentions = null;
    @SerializedName("symbols")
    @Expose
    public List<Object> symbols = null;
    private final static long serialVersionUID = -4297287215729632072L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Entities() {
    }

    /**
     * 
     * @param urls
     * @param hashtags
     * @param userMentions
     * @param symbols
     */
    public Entities(List<Object> hashtags, List<TweetHelper.Url> urls, List<Object> userMentions, List<Object> symbols) {
        super();
        this.hashtags = hashtags;
        this.urls = urls;
        this.userMentions = userMentions;
        this.symbols = symbols;
    }

}
