package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.fragments.ComposeTweetDialogFragment;
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.fragments.ReplyTweetDialogFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetDialogFragment.ComposeTweetDialogFragmentListener, ReplyTweetDialogFragment.ReplyTweetDialogFragmentListener {

    public static final String TAG = "TimelineActivity";

    public TwitterClient client;
    public List<Tweet> tweets;
    public TweetsAdapter adapter;
    public static User current;
    public ActivityTimelineBinding binding;
    EndlessRecyclerViewScrollListener scrollListener;
    View view;
    TweetDao tweetDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set ViewBinding
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        binding.fabCompose.hide();
        binding.pbLoading.setVisibility(View.VISIBLE);

        // layout of activity is stored in a special property called root
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);

        client = TwitterApp.getRestClient(this);
        tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();

        // Initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(layoutManager);
        binding.rvTweets.setAdapter(adapter);

        // Setup refresh listener which triggers new data loading
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh the list
                adapter.clear();
                populateHomeTimeline();
                binding.swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(R.color.primary_blue);

        // Endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Load more data
                loadMoreData();
            }
        };
        // Add scroll listener to RecyclerView
        binding.rvTweets.addOnScrollListener(scrollListener);

        // Query for existing tweets in the DB
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Showing data from database");
                List<TweetWithUser> tweetWithUsers = tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);
                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }
        });

        // Populate the timeline
        populateCurrentUserInfo();
        populateHomeTimeline();

        // Set up the expanded image to only show when we click a media object
        // and hide when we click it again
        binding.ivExpanded.setVisibility(View.GONE);
        binding.ivDimmer.setVisibility(View.GONE);
        binding.ivExpanded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.ivExpanded.setVisibility(View.GONE);
                binding.ivDimmer.setVisibility(View.GONE);
            }
        });
    }

    // Load more data for the timeline
    private void loadMoreData() {
        // Send an API request to get the next set of tweets
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess for loadMoreData");

                // Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception for getNextPageOfTweets", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure for loadMoreData", throwable);
            }
        }, tweets.get(tweets.size() - 1).id - 1);
    }

    private void populateCurrentUserInfo() {
        // Send an API request to get the current user's info
        client.getCurrentUserInfo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "user onSuccess!" + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    current = User.fromJson(jsonObject);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "user onFailure!" + response, throwable);
            }
        });
    }

    private void populateHomeTimeline() {
        // Send an API request to get the timeline
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "timeline onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    final List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    tweets.addAll(tweetsFromNetwork);
                    adapter.notifyDataSetChanged();
                    binding.pbLoading.setVisibility(View.GONE);
                    binding.fabCompose.show();

                    // Insert items to database
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "Saving data into database");
                            // Insert users first so our key connection works
                            List<User> usersFromNetwork = User.fromJsonTweetArray(tweetsFromNetwork);
                            tweetDao.insertModel(usersFromNetwork.toArray(new User[0]));
                            // Insert tweets next
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet[0]));
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "timeline onFailure!" + response, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Scroll back to the top
            Log.d(TAG, "pressed");
            binding.rvTweets.smoothScrollToPosition(0);
        }
        if (item.getItemId() == R.id.action_profile) {
            // Show the user's profile page
            Log.d(TAG, "user profile page clicked");
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra(User.class.getSimpleName(), Parcels.wrap(current));
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishComposeDialog(final Tweet tweet) {
        // Modify and update data
        tweets.add(0, tweet);
        adapter.notifyItemInserted(0);
        binding.rvTweets.smoothScrollToPosition(0);

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
        Snackbar.make(view, R.string.snackbar_compose_text, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_action, myOnClickListener)
                .show(); // Don’t forget to show!
    }

    // Compose a tweet when we press the button
    public void onComposeClick(View view) {
        FragmentManager fm = getSupportFragmentManager();
        ComposeTweetDialogFragment composeTweetDialogFragment = ComposeTweetDialogFragment.newInstance(current);
        composeTweetDialogFragment.show(fm, "fragment_compose_tweet");
    }

    // Enlarge a media object
    public void showEnlargedImage(String mediaUrl) {
        Glide.with(this).load(mediaUrl).into(binding.ivExpanded);
        binding.ivExpanded.setVisibility(View.VISIBLE);
        binding.ivDimmer.setVisibility(View.VISIBLE);
        binding.ivDimmer.setAlpha((float) 0.3);
    }

    @Override
    public void onFinishReplyDialog(final Tweet tweet) {
        // Modify and update data
        tweets.add(0, tweet);
        Log.d(TAG, "onFinishReplyDialog");
        adapter.notifyItemInserted(0);
        binding.rvTweets.smoothScrollToPosition(0);

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