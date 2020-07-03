package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.helpers.TimeFormatter;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetBinding;
import com.codepath.apps.restclienttemplate.fragments.ReplyTweetDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

import static com.codepath.apps.restclienttemplate.fragments.ReplyTweetDialogFragment.*;

public class TweetActivity extends AppCompatActivity implements ReplyTweetDialogFragmentListener {

    public static final String TAG = "TweetActivity";
    Tweet tweet;
    ActivityTweetBinding binding;
    TwitterClient client;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        // Set ViewBinding
        binding = ActivityTweetBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiate the client
        client = TwitterApp.getRestClient(this);

        // Set tweet info
        setTweet((Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName())));

        // Set favoriting, replying, and retweeting listeners
        binding.ibReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                if (view.getContext().getClass() == TimelineActivity.class) {
                    ReplyTweetDialogFragment replyTweetDialogFragment = newInstance(((TimelineActivity) view.getContext()).current, tweet);
                    replyTweetDialogFragment.show(fm, "fragment_reply_tweet");
                } else {
                    ReplyTweetDialogFragment replyTweetDialogFragment = newInstance(((ProfileActivity) view.getContext()).user, tweet);
                    replyTweetDialogFragment.show(fm, "fragment_reply_tweet");
                }
            }
        });

        binding.ibHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                long id = tweet.id;

                // If the tweet wasn't liked before
                if (!tweet.favorited) {
                    //Make an API call to Twitter to like the tweet
                    client.likeTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "like Tweet onSuccess");
                            try {
                                setTweet(Tweet.fromJson(json.jsonObject));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON exception when liking tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "like Tweet onFailure", throwable);
                        }
                    });
                } else {
                    //Make an API call to Twitter to unlike the tweet
                    client.unlikeTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "unlike Tweet onSuccess");
                            try {
                                setTweet(Tweet.fromJson(json.jsonObject));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON exception when unliking tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "unlike Tweet onFailure", throwable);
                        }
                    });
                }
            }
        });

        binding.ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                long id = tweet.id;

                // If the tweet wasn't retweeted before
                if (!tweet.retweeted) {
                    //Make an API call to Twitter to retweet the tweet
                    client.retweetTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "retweet Tweet onSuccess");
                            try {
                                setTweet(Tweet.fromJson(json.jsonObject));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON exception when retweeting tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "retweet Tweet onFailure", throwable);
                        }
                    });
                } else {
                    //Make an API call to Twitter to unretweet the tweet
                    client.unretweetTweet(id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "unretweet Tweet onSuccess");
                            try {
                                setTweet(Tweet.fromJson(json.jsonObject));
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON exception when unretweeting tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "unretweet Tweet onFailure", throwable);
                        }
                    });
                }
            }
        });

        View.OnClickListener tweetUser = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TweetActivity.this, ProfileActivity.class);
                i.putExtra(User.class.getSimpleName(), Parcels.wrap(tweet.user));
                startActivity(i);
            }
        };

        binding.ivProfileImage.setOnClickListener(tweetUser);
        binding.tvName.setOnClickListener(tweetUser);
        binding.tvScreenName.setOnClickListener(tweetUser);
    }

    // Set up the view
    private void setTweet(Tweet newTweet) {
        this.tweet = newTweet;
        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText("@" + tweet.user.screenName);
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(binding.ivProfileImage);

        // If we have a media attachment, we can add that
        if (tweet.mediaUrl != null) {
            Glide.with(this).load(tweet.mediaUrl).into(binding.ivMedia);
        } else {
            binding.ivMedia.setBackgroundResource(0);
            binding.ivMedia.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            Glide.with(this).clear(binding.ivMedia);
        }

        // Set the relative time and retweet/like counts
        binding.tvAbsoluteTime.setText(TimeFormatter.getTimeStamp(tweet.createdAt));
        if (tweet.retweetCount > 0) {
            binding.tvRetweets.setText(TweetsAdapter.format(tweet.retweetCount));
        } else {
            binding.tvRetweets.setText("");
        }
        if (tweet.likeCount > 0) {
            binding.tvLikes.setText(TweetsAdapter.format(tweet.likeCount));
        } else {
            binding.tvLikes.setText("");
        }

        // Show that the tweet was retweeted/liked by the user
        if (tweet.favorited) {
            binding.ibHeart.setSelected(true);
        } else {
            binding.ibHeart.setSelected(false);
        }
        if (tweet.retweeted) {
            binding.ibRetweet.setSelected(true);
        } else {
            binding.ibRetweet.setSelected(false);
        }
    }

    // Return when the back arrow is pressed
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFinishReplyDialog(final Tweet tweet) {
        // Define the click listener as a member
        View.OnClickListener myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent for the new activity
                Intent i = new Intent(getApplicationContext(), TweetActivity.class);
                i.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                startActivity(i);
            }
        };

        // Pass in the click listener when displaying the Snackbar
        Snackbar.make(view, R.string.snackbar_reply_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, myOnClickListener)
                .show(); // Don’t forget to show!
    }
}