package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.ProfileActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public static final String TAG = "UserAdapter";
    Context context;
    List<User> users;
    TwitterClient client;

    // Pass in the context and list of users
    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    // For each row, inflate the layout for the Tweet
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        client = TwitterApp.getRestClient(context);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        User user = users.get(position);
        // Bind the user with view holder
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        User user;
        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvName;
        Button btnFollow;
        boolean following;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            itemView.setOnClickListener(this);
        }

        public void bind(final User user) {
            this.user = user;
            tvName.setText(user.name);
            tvScreenName.setText("@" + user.screenName);
            Glide.with(context).load(user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);

            // See if the current user is following this person
            if (!user.screenName.equals(TimelineActivity.current.screenName)) {
                client.isFollowing(TimelineActivity.current.screenName, user.screenName, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            btnFollow.setFocusableInTouchMode(true);
                            following = json.jsonObject.getJSONObject("relationship").getJSONObject("target").getBoolean("followed_by");
                            btnFollow.setVisibility(View.VISIBLE);
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
                btnFollow.setVisibility(View.GONE);
            }

            btnFollow.setOnClickListener(new View.OnClickListener() {
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

        private void setupFollowStatus() {
            if (following) {
                btnFollow.setSelected(true);
                btnFollow.setTextColor(ContextCompat.getColor(context, R.color.white));
                btnFollow.setText("FOLLOWING");
            } else {
                btnFollow.setSelected(false);
                btnFollow.setTextColor(ContextCompat.getColor(context, R.color.primary_blue));
                btnFollow.setText("FOLLOW");
            }
        }

        // When the user clicks on a row, show the profile for the selected user
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // Gets the item position

            // Make sure the position is valid
            if (position != RecyclerView.NO_POSITION) {
                User current = users.get(position);

                // Create an intent for the new activity
                Log.d(TAG, "user profile page clicked");
                Intent i = new Intent(context, ProfileActivity.class);
                i.putExtra(User.class.getSimpleName(), Parcels.wrap(current));
                context.startActivity(i);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<User> list) {
        users.addAll(list);
        notifyDataSetChanged();
    }
}
