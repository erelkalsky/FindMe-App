package com.example.findme.classes.cases;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.example.findme.home.cases.aCase.CaseActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class CaseUserAdapter extends RecyclerView.Adapter<CaseUserAdapter.CaseViewHolder> {
    private List<Case> casesList;
    private List<String> caseListId;
    private Context context;

    public CaseUserAdapter(List<Case> casesList, List<String> caseListId, Context context) {
        this.casesList = casesList;
        this.caseListId = caseListId;
        this.context = context;
    }

    @NonNull
    @Override
    public CaseUserAdapter.CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_users_list, parent, false);
        return new CaseUserAdapter.CaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaseUserAdapter.CaseViewHolder holder, int position) {
        Case aCase = casesList.get(position);

        holder.tvTitle.setText(aCase.getCaseId());
        String str = aCase.getMissingPerson().getFirstName() + " " + aCase.getMissingPerson().getLastName() + " (ת\"ז: " + aCase.getMissingPerson().getId() + ")";
        holder.tvDescription.setText(str);

        holder.itemView.setOnClickListener(view -> {
            context.startActivity(new Intent(context, CaseActivity.class)
                    .putExtra("case", aCase)
                    .putExtra("caseId", caseListId.get(position))
            );
            ((Activity) context).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        FirebaseStorage.getInstance().getReference("cases").child(caseListId.get(position)).child("caseImage")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.ivImage);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.ivImage.setImageResource(R.drawable.img_default_profile);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return casesList.size() != 0 ? casesList.size() : 0;
    }
    class CaseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView ivImage;

        public CaseViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvUserName);
            tvDescription = itemView.findViewById(R.id.tvUserRole);
            ivImage = itemView.findViewById(R.id.ivUserImage);
        }
    }
}
