
package TweetHelper;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User_ implements Serializable
{

    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("screen_name")
    @Expose
    public String screenName;
    @SerializedName("location")
    @Expose
    public String location;
    @SerializedName("url")
    @Expose
    public Object url;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("translator_type")
    @Expose
    public String translatorType;
    @SerializedName("protected")
    @Expose
    public Boolean _protected;
    @SerializedName("verified")
    @Expose
    public Boolean verified;
    @SerializedName("followers_count")
    @Expose
    public Integer followersCount;
    @SerializedName("friends_count")
    @Expose
    public Integer friendsCount;
    @SerializedName("listed_count")
    @Expose
    public Integer listedCount;
    @SerializedName("favourites_count")
    @Expose
    public Integer favouritesCount;
    @SerializedName("statuses_count")
    @Expose
    public Integer statusesCount;
    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("utc_offset")
    @Expose
    public Object utcOffset;
    @SerializedName("time_zone")
    @Expose
    public Object timeZone;
    @SerializedName("geo_enabled")
    @Expose
    public Boolean geoEnabled;
    @SerializedName("lang")
    @Expose
    public Object lang;
    @SerializedName("contributors_enabled")
    @Expose
    public Boolean contributorsEnabled;
    @SerializedName("is_translator")
    @Expose
    public Boolean isTranslator;
    @SerializedName("profile_background_color")
    @Expose
    public String profileBackgroundColor;
    @SerializedName("profile_background_image_url")
    @Expose
    public String profileBackgroundImageUrl;
    @SerializedName("profile_background_image_url_https")
    @Expose
    public String profileBackgroundImageUrlHttps;
    @SerializedName("profile_background_tile")
    @Expose
    public Boolean profileBackgroundTile;
    @SerializedName("profile_link_color")
    @Expose
    public String profileLinkColor;
    @SerializedName("profile_sidebar_border_color")
    @Expose
    public String profileSidebarBorderColor;
    @SerializedName("profile_sidebar_fill_color")
    @Expose
    public String profileSidebarFillColor;
    @SerializedName("profile_text_color")
    @Expose
    public String profileTextColor;
    @SerializedName("profile_use_background_image")
    @Expose
    public Boolean profileUseBackgroundImage;
    @SerializedName("profile_image_url")
    @Expose
    public String profileImageUrl;
    @SerializedName("profile_image_url_https")
    @Expose
    public String profileImageUrlHttps;
    @SerializedName("profile_banner_url")
    @Expose
    public String profileBannerUrl;
    @SerializedName("default_profile")
    @Expose
    public Boolean defaultProfile;
    @SerializedName("default_profile_image")
    @Expose
    public Boolean defaultProfileImage;
    @SerializedName("following")
    @Expose
    public Object following;
    @SerializedName("follow_request_sent")
    @Expose
    public Object followRequestSent;
    @SerializedName("notifications")
    @Expose
    public Object notifications;
    private final static long serialVersionUID = 6795637485727181726L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public User_() {
    }

    /**
     * 
     * @param geoEnabled
     * @param description
     * @param profileTextColor
     * @param screenName
     * @param contributorsEnabled
     * @param profileUseBackgroundImage
     * @param createdAt
     * @param profileBackgroundImageUrl
     * @param _protected
     * @param id
     * @param lang
     * @param profileImageUrl
     * @param defaultProfileImage
     * @param idStr
     * @param profileSidebarBorderColor
     * @param statusesCount
     * @param utcOffset
     * @param profileBackgroundTile
     * @param profileBackgroundImageUrlHttps
     * @param verified
     * @param profileBackgroundColor
     * @param favouritesCount
     * @param timeZone
     * @param friendsCount
     * @param defaultProfile
     * @param profileLinkColor
     * @param isTranslator
     * @param listedCount
     * @param url
     * @param profileBannerUrl
     * @param profileSidebarFillColor
     * @param translatorType
     * @param profileImageUrlHttps
     * @param following
     * @param name
     * @param location
     * @param followersCount
     * @param notifications
     * @param followRequestSent
     */
    public User_(Integer id, String idStr, String name, String screenName, String location, Object url, String description, String translatorType, Boolean _protected, Boolean verified, Integer followersCount, Integer friendsCount, Integer listedCount, Integer favouritesCount, Integer statusesCount, String createdAt, Object utcOffset, Object timeZone, Boolean geoEnabled, Object lang, Boolean contributorsEnabled, Boolean isTranslator, String profileBackgroundColor, String profileBackgroundImageUrl, String profileBackgroundImageUrlHttps, Boolean profileBackgroundTile, String profileLinkColor, String profileSidebarBorderColor, String profileSidebarFillColor, String profileTextColor, Boolean profileUseBackgroundImage, String profileImageUrl, String profileImageUrlHttps, String profileBannerUrl, Boolean defaultProfile, Boolean defaultProfileImage, Object following, Object followRequestSent, Object notifications) {
        super();
        this.id = id;
        this.idStr = idStr;
        this.name = name;
        this.screenName = screenName;
        this.location = location;
        this.url = url;
        this.description = description;
        this.translatorType = translatorType;
        this._protected = _protected;
        this.verified = verified;
        this.followersCount = followersCount;
        this.friendsCount = friendsCount;
        this.listedCount = listedCount;
        this.favouritesCount = favouritesCount;
        this.statusesCount = statusesCount;
        this.createdAt = createdAt;
        this.utcOffset = utcOffset;
        this.timeZone = timeZone;
        this.geoEnabled = geoEnabled;
        this.lang = lang;
        this.contributorsEnabled = contributorsEnabled;
        this.isTranslator = isTranslator;
        this.profileBackgroundColor = profileBackgroundColor;
        this.profileBackgroundImageUrl = profileBackgroundImageUrl;
        this.profileBackgroundImageUrlHttps = profileBackgroundImageUrlHttps;
        this.profileBackgroundTile = profileBackgroundTile;
        this.profileLinkColor = profileLinkColor;
        this.profileSidebarBorderColor = profileSidebarBorderColor;
        this.profileSidebarFillColor = profileSidebarFillColor;
        this.profileTextColor = profileTextColor;
        this.profileUseBackgroundImage = profileUseBackgroundImage;
        this.profileImageUrl = profileImageUrl;
        this.profileImageUrlHttps = profileImageUrlHttps;
        this.profileBannerUrl = profileBannerUrl;
        this.defaultProfile = defaultProfile;
        this.defaultProfileImage = defaultProfileImage;
        this.following = following;
        this.followRequestSent = followRequestSent;
        this.notifications = notifications;
    }

}
