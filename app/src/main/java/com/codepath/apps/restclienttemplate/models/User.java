package com.codepath.apps.restclienttemplate.models;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class User {

    public String name;
    public String screenName;
    public String profileImageUrl;
    public String profileBannerUrl;

    // No-arg, empty constructor required for Parceler
    public User() {
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Return a user from a JSON object
    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        String url = jsonObject.getString("profile_image_url_https");
        String extension = FilenameUtils.getExtension(url);
        user.profileImageUrl = url.substring(0, url.indexOf("_normal")) + "_bigger." + extension;
        user.profileBannerUrl = jsonObject.has("profile_banner_url") ? jsonObject.getString("profile_banner_url") : null;
        return user;
    }

    // Create a list of Users from a JSON array
    public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            users.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return users;
    }
}
