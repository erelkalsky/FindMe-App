package com.example.findme.classes.users;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.home.users.UsersFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private List<String> userListId;
    private Context context;
    private String filter;
    private UserAdapter.OnUserClickListener onUserClickListener;
    public UserAdapter(List<User> userList, List<String> userListId, Context context, UsersFragment fragmentContext) {
        this.userList = userList;
        this.userListId = userListId;
        this.context = context;
        this.onUserClickListener = (UserAdapter.OnUserClickListener) fragmentContext;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_users_list, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

        if(!(filter == null || filter.isEmpty() ||
                user.getPhone().startsWith(filter) ||
                user.getFirstName().startsWith(filter) ||
                user.getLastName().startsWith(filter) ||
                (user.getFirstName() + " " + user.getLastName()).startsWith(filter))) {


            layoutParams.width = 0;
            layoutParams.height = 0;
            layoutParams.setMargins(0, 0, 0, 0);
            holder.itemView.setLayoutParams(layoutParams);
            holder.itemView.setVisibility(View.GONE);

            return;
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.setMargins(5, 5, 5, 5);
            holder.itemView.setLayoutParams(layoutParams);
            holder.itemView.setVisibility(View.VISIBLE);
        }

        if (user.getPhone().equals(Database.user.getPhone())) {
            holder.tvUserName.setText(user.getFirstName() + " " + user.getLastName() + " (אני)");
        } else {
            holder.tvUserName.setText(user.getFirstName() + " " + user.getLastName());
        }

        holder.tvUserRole.setText(Database.roleToString(context, user.getRole()) + " (טלפון: " + user.getPhone() + ")");

        FirebaseStorage.getInstance().getReference("users").child(userListId.get(position))
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.ivUserImage);

                    }
        });

        holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user, userListId.get(position)));
    }

    @Override
    public int getItemCount() {
        return userList.size() != 0 ? userList.size() : 0;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRole;
        ImageView ivUserImage;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            ivUserImage = itemView.findViewById(R.id.ivUserImage);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(User user, String userId);
    }
}
