package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.fragments.ReplyTweetDialogFragment;
import com.codepath.apps.restclienttemplate.fragments.TweetFragment;
import com.codepath.apps.restclienttemplate.fragments.UserFragment;
import com.codepath.apps.restclienttemplate.adapters.ViewPagerAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityProfileBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ProfileActivity extends AppCompatActivity implements ReplyTweetDialogFragment.ReplyTweetDialogFragmentListener {

    public static final String TAG = "ProfileActivity";
    public User user;
    public ActivityProfileBinding binding;
    View view;
    public static TwitterClient client;
    public boolean following;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set ViewBinding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.htabToolbar);

        // Set user info
        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));
        Glide.with(getApplicationContext()).load(user.profileImageUrl).transform(new CircleCrop()).into(binding.ivProfileImage);
        Glide.with(getApplicationContext()).load(user.profileBannerUrl).into(binding.htabHeader);

        getSupportActionBar().setTitle(user.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViewPager();

        client = TwitterApp.getRestClient(this);

        binding.htabTabs.setupWithViewPager(binding.htabViewpager);

        binding.htabTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Change tabs accordingly
                binding.htabViewpager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        //
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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

        if (!user.screenName.equals(TimelineActivity.current.screenName)) {
            client.isFollowing(TimelineActivity.current.screenName, user.screenName, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    try {
                        binding.btnFollowing.setFocusableInTouchMode(true);
                        following = json.jsonObject.getJSONObject("relationship").getJSONObject("target").getBoolean("followed_by");
                        binding.btnFollowing.setVisibility(View.VISIBLE);
                        setupFollowStatus();
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception for isFollowing", e);
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                }
            });
        } else {
            binding.btnFollowing.setVisibility(View.GONE);
        }

        binding.btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (following) {
                    client.unfollowUser(user.screenName, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            following = false;
                            setupFollowStatus();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "unfollowing onFailure", throwable);
                        }
                    });
                } else {
                    client.followUser(user.screenName, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            following = true;
                            setupFollowStatus();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "following onFailure", throwable);
                        }
                    });
                }
            }
        });
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(
                getSupportFragmentManager());
        adapter.addFrag(TweetFragment.newInstance(user), "Tweets");
        adapter.addFrag(UserFragment.newInstance(user, "Following"), "Following");
        adapter.addFrag(UserFragment.newInstance(user, "Followers"), "Followers");
        binding.htabViewpager.setAdapter(adapter);
    }

    private void setupFollowStatus() {
        if (following) {
            binding.btnFollowing.setSelected(true);
            binding.btnFollowing.setTextColor(ContextCompat.getColor(this, R.color.white));
            binding.btnFollowing.setText("FOLLOWING");
        } else {
            binding.btnFollowing.setSelected(false);
            binding.btnFollowing.setTextColor(ContextCompat.getColor(this, R.color.primary_blue));
            binding.btnFollowing.setText("FOLLOW");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        ((TweetFragment) ((ViewPagerAdapter) binding.htabViewpager.getAdapter()).getItem(0)).tweets.add(0, tweet);
        Log.d(TAG, "onFinishReplyDialog");
        ((TweetFragment) ((ViewPagerAdapter) binding.htabViewpager.getAdapter()).getItem(0)).adapter.notifyItemInserted(0);
        ((TweetFragment) ((ViewPagerAdapter) binding.htabViewpager.getAdapter()).getItem(0)).rvTweets.smoothScrollToPosition(0);

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