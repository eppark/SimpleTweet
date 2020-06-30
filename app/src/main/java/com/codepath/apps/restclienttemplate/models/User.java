package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class User {

    public String name;
    public String screenName;
    public String profileImageUrl;

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
        String extension = url.substring(url.length() - 4);
        user.profileImageUrl = url.substring(0, url.length() - 10) + "bigger" + extension;
        return user;
    }
}
