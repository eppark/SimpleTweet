package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.codepath.apps.restclienttemplate.models.ProfileActivity;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public static final String TAG = "UserAdapter";
    Context context;
    List<User> users;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
        }

        public void bind(User user) {
            this.user = user;
            tvName.setText(user.name);
            tvScreenName.setText("@" + user.screenName);
            Glide.with(context).load(user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);
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
