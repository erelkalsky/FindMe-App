package com.example.findme.classes.cases;

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
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class UsersInCaseAdapter extends RecyclerView.Adapter<UsersInCaseAdapter.UserViewHolder> {
    private List<String> userListId;
    private Context context;
    private String filter;
    private OnUsersInCaseClickListener onUsersInCaseClickListener;

    public UsersInCaseAdapter(List<String> userListId, Context context) {
        this.userListId = userListId;
        this.context = context;
        this.onUsersInCaseClickListener = (OnUsersInCaseClickListener) context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_users_list, parent, false);
        return new UsersInCaseAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userListId.get(position))
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);

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

                            if(Database.user != null) {
                                if(Database.user.getRole() == 1) {
                                    holder.itemView.findViewById(R.id.mainFrame).setForeground(null);
                                } else {
                                    holder.itemView.setOnClickListener(v -> onUsersInCaseClickListener.onUserClick(user, document.getId(), holder.itemView.findViewById(R.id.mainFrame)));
                                }
                            }
                        } else {
                            layoutParams.width = 0;
                            layoutParams.height = 0;
                            layoutParams.setMargins(0, 0, 0, 0);
                            holder.itemView.setLayoutParams(layoutParams);
                            holder.itemView.setVisibility(View.GONE);
                        }
                    }
                });
    }


    @Override
    public int getItemCount() {
        return userListId.size() != 0 ? userListId.size() : 0;
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

    public interface OnUsersInCaseClickListener {
        void onUserClick(User user, String userId, View view);
    }
}
