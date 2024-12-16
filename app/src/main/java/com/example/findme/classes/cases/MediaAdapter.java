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
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    private List<StorageReference> items;
    private Context context;
    private OnMediaClickListener onMediaClickListener;

    public MediaAdapter(List<StorageReference> items, Context context) {
        this.items = items;
        this.context = context;
        this.onMediaClickListener = (OnMediaClickListener) context;
    }

    @NonNull
    @Override
    public MediaAdapter.MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.MediaViewHolder holder, int position) {
        StorageReference item = items.get(position);

        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.ivImage);

                if(item.getName().endsWith(".mp4")) {
                    holder.itemView.findViewById(R.id.cvItem).setOnClickListener(v -> onMediaClickListener.onMediaClick(uri, true));
                    holder.itemView.findViewById(R.id.ivPlay).setVisibility(View.VISIBLE);
                } else {
                    holder.itemView.findViewById(R.id.cvItem).setOnClickListener(v -> onMediaClickListener.onMediaClick(uri, false));
                }
            }
        });

        item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                holder.tvDescription.setText(storageMetadata.getCustomMetadata("description"));

                FirebaseFirestore.getInstance().collection("users")
                        .document(storageMetadata.getCustomMetadata("userId"))
                        .get(Source.SERVER)
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot document) {
                                String userName = "המשתמש אינו קיים";
                                if (document != null && document.exists()) {
                                    User user = document.toObject(User.class);
                                    userName = user.getFirstName() + " " + user.getLastName();
                                }

                                holder.tvFooter.setText("● הועלה על ידי: " + userName);
                            }
                        });

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size() != 0 ? items.size() : 0;
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvFooter;
        ImageView ivImage;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvFooter = itemView.findViewById(R.id.tvFooter);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
    }

    public interface OnMediaClickListener {
        void onMediaClick(Uri uri, boolean isVideo);
    }
}
