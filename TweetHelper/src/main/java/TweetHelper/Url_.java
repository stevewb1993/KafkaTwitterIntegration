
package TweetHelper;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Url_ implements Serializable
{

    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("expanded_url")
    @Expose
    public String expandedUrl;
    @SerializedName("display_url")
    @Expose
    public String displayUrl;
    @SerializedName("indices")
    @Expose
    public List<Integer> indices = null;
    private final static long serialVersionUID = 4310913709946635863L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Url_() {
    }

    /**
     * 
     * @param displayUrl
     * @param indices
     * @param expandedUrl
     * @param url
     */
    public Url_(String url, String expandedUrl, String displayUrl, List<Integer> indices) {
        super();
        this.url = url;
        this.expandedUrl = expandedUrl;
        this.displayUrl = displayUrl;
        this.indices = indices;
    }

}
