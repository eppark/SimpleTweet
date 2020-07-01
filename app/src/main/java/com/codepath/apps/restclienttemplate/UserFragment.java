package com.codepath.apps.restclienttemplate;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TweetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    public static final String TAG = "UserFragment";
    User user;
    RecyclerView rvUsers;
    UserAdapter adapter;
    List<User> users;
    SwipeRefreshLayout swipeContainer;
    TwitterClient client;
    EndlessRecyclerViewScrollListener scrollListener;
    String id;
    long cursor;

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance(User user, String id) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
        args.putParcelable("id", Parcels.wrap(id));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the user
        user = Parcels.unwrap(getArguments().getParcelable("user"));
        id = Parcels.unwrap(getArguments().getParcelable("id"));
        users = new ArrayList<>();
        cursor = -1;

        // Recycler view setup: layout manager and the adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        rvUsers = (RecyclerView) getView().findViewById(R.id.rvTweets);
        rvUsers.setLayoutManager(layoutManager);
        adapter = new UserAdapter(this.getContext(), users);
        rvUsers.setAdapter(adapter);

        // Setup refresh listener which triggers new data loading
        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh the list
                cursor = -1;
                adapter.clear();
                if (id.equals("Followers")) {
                    populateFollowers();
                } else {
                    populateFollowing();
                }
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.primary_blue);

        // Endless scrolling
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Load more data
                if (id.equals("Followers")) {
                    populateFollowers();
                } else {
                    populateFollowing();
                }
            }
        };
        // Add scroll listener to RecyclerView
        rvUsers.addOnScrollListener(scrollListener);

        // Populate the timeline
        client = TwitterApp.getRestClient(getContext());
        if (id.equals("Followers")) {
            populateFollowers();
        } else {
            populateFollowing();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tweet, container, false);
    }

    // Get following
    private void populateFollowing() {
        // Send an API request to get the followers
        client.getFollowing(user.screenName, cursor, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "following onSuccess!" + json.toString());
                JSONArray jsonArray = null;
                try {
                    jsonArray = json.jsonObject.getJSONArray("ids");
                    getUsers(jsonArray);
                    cursor = json.jsonObject.getLong("next_cursor");
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception for users", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "following onFailure!" + response, throwable);
            }
        });
    }

    // Get followers
    private void populateFollowers() {
        // Send an API request to get the followers
        client.getFollowers(user.screenName, cursor, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "followers onSuccess!" + json.toString());
                JSONArray jsonArray = null;
                try {
                    jsonArray = json.jsonObject.getJSONArray("ids");
                    getUsers(jsonArray);
                    cursor = json.jsonObject.getLong("next_cursor");
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception for users", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "followers onFailure!" + response, throwable);
            }
        });
    }

    private void getUsers(JSONArray jsonArray) {
        String ids = jsonArray.toString().replace("[", "").replace("]","");
        Log.d(TAG, "this is " + ids);
        // Send an API request to get the users
        client.getUsers(ids, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "users onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.addAll(User.fromJsonArray(jsonArray));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "users onFailure!" + response, throwable);
            }
        });
    }
}