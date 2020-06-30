package com.codepath.apps.restclienttemplate.models;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String mediaUrl;
    public long id;

    // Create a Tweet from a JSON object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = htmlToString(jsonObject.getString("text"));
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.mediaUrl = jsonObject.getJSONObject("entities").has("media") ? jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https") : null;
        tweet.id = jsonObject.getLong("id");
        return tweet;
    }

    // Create a list of Tweets from a JSON array
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // Convert HTML to String
    public static String htmlToString(String htmlCode) {
        return Html.fromHtml(htmlCode).toString();
    }
}
