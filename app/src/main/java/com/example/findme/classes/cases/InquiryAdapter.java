package com.example.findme.classes.cases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findme.R;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.List;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder> {
    private List<Inquiry> items;
    private Context context;
    private InquiryAdapter.OnInquiryClickListener onInquiryClickListener;

    public InquiryAdapter(List<Inquiry> items, Context context) {
        this.items = items;
        this.context = context;
        this.onInquiryClickListener = (OnInquiryClickListener) context;
    }

    @NonNull
    @Override
    public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_inquery, parent, false);
        return new InquiryAdapter.InquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InquiryViewHolder holder, int position) {
        Inquiry inquiry = items.get(position);

        holder.tvTitle.setText(inquiry.getInvestigatedName());

        FirebaseFirestore.getInstance().collection("users")
                .document(inquiry.getUserId())
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        String userName = "המשתמש אינו קיים";
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            userName = user.getFirstName() + " " + user.getLastName();
                        }

                        holder.tvFooter.setText("● תושאל על ידי: " + userName);
                    }
                });

        holder.itemView.setOnClickListener(v -> onInquiryClickListener.onInquiryClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size() != 0 ? items.size() : 0;
    }

    class InquiryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvFooter;

        public InquiryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvFooter = itemView.findViewById(R.id.tvFooter);
        }
    }

    public interface OnInquiryClickListener {
        void onInquiryClick(int position);
    }
}
