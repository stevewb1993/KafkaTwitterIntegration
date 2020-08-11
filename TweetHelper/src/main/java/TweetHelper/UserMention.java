
package TweetHelper;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserMention implements Serializable
{

    @SerializedName("screen_name")
    @Expose
    public String screenName;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("indices")
    @Expose
    public List<Integer> indices = null;
    private final static long serialVersionUID = -1145533690852235527L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public UserMention() {
    }

    /**
     * 
     * @param idStr
     * @param indices
     * @param name
     * @param screenName
     * @param id
     */
    public UserMention(String screenName, String name, Integer id, String idStr, List<Integer> indices) {
        super();
        this.screenName = screenName;
        this.name = name;
        this.id = id;
        this.idStr = idStr;
        this.indices = indices;
    }

}
