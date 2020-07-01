package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.TimelineActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public static final String TAG = "TweetsAdapter";
    Context context;
    List<Tweet> tweets;
    float scale;
    TwitterClient client;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        this.scale = context.getResources().getDisplayMetrics().density;
    }

    // For each row, inflate the layout for the Tweet
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        client = TwitterApp.getRestClient(context);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements ReplyTweetDialogFragment.ReplyTweetDialogFragmentListener {

        Tweet tweet;
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvName;
        TextView tvRelativeTime;
        ImageView ivMedia;
        TextView tvRetweets;
        TextView tvLikes;
        ImageButton ibRetweet;
        ImageButton ibReply;
        ImageButton ibHeart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvRelativeTime = itemView.findViewById(R.id.tvRelativeTime);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            tvRetweets = itemView.findViewById(R.id.tvRetweets);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            ibHeart = itemView.findViewById(R.id.ibHeart);
            ibReply = itemView.findViewById(R.id.ibReply);
            ibRetweet = itemView.findViewById(R.id.ibRetweet);

            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = ((TimelineActivity) view.getContext()).getSupportFragmentManager();
                    ReplyTweetDialogFragment replyTweetDialogFragment = ReplyTweetDialogFragment.newInstance(((TimelineActivity) view.getContext()).current, tweet);
                    replyTweetDialogFragment.show(fm, "fragment_compose_tweet");
                }
            });

            ibHeart.setOnClickListener(new View.OnClickListener() {
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
                                    tweets.set(tweets.indexOf(tweet), Tweet.fromJson(json.jsonObject));
                                    ((TimelineActivity) view.getContext()).adapter.notifyDataSetChanged();
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
                                    tweets.set(tweets.indexOf(tweet), Tweet.fromJson(json.jsonObject));
                                    ((TimelineActivity) view.getContext()).adapter.notifyDataSetChanged();
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

            ibRetweet.setOnClickListener(new View.OnClickListener() {
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
                                    tweets.set(tweets.indexOf(tweet), Tweet.fromJson(json.jsonObject));
                                    ((TimelineActivity) view.getContext()).adapter.notifyDataSetChanged();
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
                                    tweets.set(tweets.indexOf(tweet), Tweet.fromJson(json.jsonObject));
                                    ((TimelineActivity) view.getContext()).adapter.notifyDataSetChanged();
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
        }

        public void bind(Tweet tweet) {
            this.tweet = tweet;
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText("@" + tweet.user.screenName);
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);

            // If we have a media attachment, we can add that
            if (tweet.mediaUrl != null) {
                ivMedia.getLayoutParams().height = (int) (200 * scale + 0.5f);
                ivMedia.setBackgroundResource(R.drawable.border);
                ivMedia.setPadding(4, 4, 4, 4);
                Glide.with(context).load(tweet.mediaUrl).apply(new RequestOptions()
                        .transform(new CenterCrop(), new RoundedCorners(40))).into(ivMedia);
            } else {
                ivMedia.setBackgroundResource(0);
                ivMedia.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                Glide.with(context).clear(ivMedia);
            }

            // Set the relative time and retweet/like counts
            tvRelativeTime.setText(TimeFormatter.getTimeDifference(tweet.createdAt));
            if (tweet.retweetCount > 0) {
                tvRetweets.setText(format(tweet.retweetCount));
            } else {
                tvRetweets.setText("");
            }
            if (tweet.likeCount > 0) {
                tvLikes.setText(format(tweet.likeCount));
            } else {
                tvLikes.setText("");
            }

            // Show that the tweet was retweeted/liked by the user
            if (tweet.favorited) {
                ibHeart.setSelected(true);
            } else {
                ibHeart.setSelected(false);
            }
            if (tweet.retweeted) {
                ibRetweet.setSelected(true);
            } else {
                ibRetweet.setSelected(false);
            }
        }

        @Override
        public void onFinishReplyDialog(Tweet tweet) {
            // Modify and update data
            tweets.add(0, tweet);
            notifyItemInserted(0);
            ((TimelineActivity) context.getApplicationContext()).binding.rvTweets.smoothScrollToPosition(0);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // Truncate counts in a readable format
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value);

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
