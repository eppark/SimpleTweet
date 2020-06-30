package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

public class ComposeTweetDialogFragment extends DialogFragment {

    private EditText etCompose;
    private ImageView ivProfilePicture;

    public ComposeTweetDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    public static ComposeTweetDialogFragment newInstance(User current) {
        ComposeTweetDialogFragment frag = new ComposeTweetDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(current));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_tweet, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        User current = Parcels.unwrap(getArguments().getParcelable("user"));
        String url = current.profileImageUrl != null ? current.profileImageUrl : "https://abs.twimg.com/sticky/default_profile_images/default_profile_normal.png";
        Glide.with(this).load(url).transform(new CircleCrop()).into(ivProfilePicture);

        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
