package com.codepath.apps.restclienttemplate.models;

import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String mediaUrl;
    public long id;
    public long retweetCount;
    public long likeCount;
    public boolean favorited;
    public boolean retweeted;
    public User retweetedBy;

    // No-arg, empty constructor required for Parceler
    public Tweet() {

    }

    // Create a Tweet from a JSON object
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        // If this tweet is an original Tweet
        Tweet tweet = new Tweet();
        if (!jsonObject.has("retweeted_status")) {
            tweet = createTweet(jsonObject);
            tweet.retweetedBy = null;
        } else {
            // This tweet isn't an original tweet, so we need to get the original Tweet's data
            tweet = createTweet(jsonObject.getJSONObject("retweeted_status"));
            tweet.retweetedBy = User.fromJson(jsonObject.getJSONObject("user"));
        }
        return tweet;
    }

    // Assign Tweet urls
    public static Tweet createTweet(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = htmlToString(jsonObject.getString("text"));
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.mediaUrl = jsonObject.getJSONObject("entities").has("media") ? jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url_https") : null;
        tweet.id = jsonObject.getLong("id");
        tweet.likeCount = jsonObject.getLong("favorite_count");
        tweet.retweetCount = jsonObject.getLong("retweet_count");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
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
