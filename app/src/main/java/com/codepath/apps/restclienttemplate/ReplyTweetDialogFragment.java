package com.codepath.apps.restclienttemplate;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyTweetDialogFragment extends DialogFragment {

    public static final String TAG = "ReplyTweetDialogFrag";
    public static final Integer MAX_TWEET_LENGTH = 280;

    private EditText etCompose;
    private ImageView ivProfilePicture;
    private Button btnReply;
    private TextView tvCharacterCount;
    private TextView tvReplyingTo;

    TwitterClient client;
    Tweet tweet;
    Tweet original;

    public interface ReplyTweetDialogFragmentListener {
        void onFinishReplyDialog(Tweet tweet);
    }

    public ReplyTweetDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static ReplyTweetDialogFragment newInstance(User current, Tweet orig) {
        ReplyTweetDialogFragment frag = new ReplyTweetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(current));
        args.putParcelable("tweet", Parcels.wrap(orig));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_tweet, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        User current = Parcels.unwrap(getArguments().getParcelable("user"));
        original = Parcels.unwrap(getArguments().getParcelable("tweet"));
        String url = current.profileImageUrl != null ? current.profileImageUrl : "https://abs.twimg.com/sticky/default_profile_images/default_profile_bigger.png";
        Log.d(TAG, "original" + original.user.screenName);
        // Set components
        client = TwitterApp.getRestClient(getContext());
        etCompose = (EditText) view.findViewById(R.id.etCompose);
        ivProfilePicture = (ImageView) view.findViewById(R.id.ivProfilePicture);
        tvCharacterCount = (TextView) view.findViewById(R.id.tvCharacterCount);
        tvCharacterCount.setText(MAX_TWEET_LENGTH.toString());
        tvReplyingTo = (TextView) view.findViewById(R.id.tvReplyingTo);
        tvReplyingTo.setText("Replying to @" + original.user.screenName);
        btnReply = (Button) view.findViewById(R.id.btnReply);
        btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.lighter_blue));
        btnReply.setEnabled(false);

        Glide.with(this).load(url).transform(new CircleCrop()).into(ivProfilePicture);

        // Set text listener on character counter
        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String counter = ((Integer) (MAX_TWEET_LENGTH - editable.toString().length())).toString();
                tvCharacterCount.setText(counter);
                if (Integer.parseInt(counter) <= 0) {
                    tvCharacterCount.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.lighter_blue));
                    btnReply.setEnabled(false);
                } else if (Integer.parseInt(counter) <= 20) {
                    tvCharacterCount.setTextColor(ContextCompat.getColor(getContext(), R.color.orange));
                    btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.primary_blue));
                    btnReply.setEnabled(true);
                }
                else {
                    tvCharacterCount.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_gray));
                    btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.primary_blue));
                    btnReply.setEnabled(true);
                }
            }
        });

        // Set click listener on button
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tweetContent = etCompose.getText().toString();
                //Make an API call to Twitter to reply to the tweet
                client.publishTweet(tweetContent, original.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "reply Tweet onSuccess");
                        try {
                            tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Replied tweet says: " + tweet.body);

                            // Return input text back to activity through the implemented listener
                            ReplyTweetDialogFragmentListener listener = (ReplyTweetDialogFragmentListener) getActivity();
                            listener.onFinishReplyDialog(tweet);
                            // Close the dialog and return back to the parent activity
                            dismiss();
                        } catch (JSONException e) {
                            Log.e(TAG, "reply tweet error", e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "reply Tweet onFailure", throwable);
                    }
                });
            }
        });

        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 95% of the screen width
        window.setLayout((int) (size.x * 0.95), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();
    }


}
