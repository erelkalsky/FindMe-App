package com.example.findme.home;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;

import com.example.findme.classes.cases.CaseUserAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    View view;
    List<Case> caseList = new ArrayList<>();
    List<String> caseListId = new ArrayList<>();
    CaseUserAdapter adapter;
    RecyclerView recyclerView;
    TextView tvUserName, tvNoCases;
    ImageView ivUserImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCases);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CaseUserAdapter(caseList, caseListId, requireContext());
        recyclerView.setAdapter(adapter);

        tvUserName = view.findViewById(R.id.tvUserName);
        ivUserImage = view.findViewById(R.id.ivUserImage);

        tvNoCases = view.findViewById(R.id.tvNoCases);

        updateUi();

        return view;
    }

    private void updateCaseList() {
        caseList.clear();
        caseListId.clear();

        FirebaseFirestore.getInstance().collection("cases")
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Case aCase = document.toObject(Case.class);

                            if(aCase.isActive() && aCase.isUserInCase(Database.userId)) {
                                caseList.add(aCase);
                                caseListId.add(document.getId());
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if(caseList.size() == 0) {
                            recyclerView.setVisibility(View.GONE);
                            tvNoCases.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoCases.setVisibility(View.GONE);
                        }
                    }
                });
    }

    public void updateUi() {
        tvUserName.setText("שלום " + Database.user.getFirstName() + " " + Database.user.getLastName());

        FirebaseStorage.getInstance().getReference("users").child(Database.userId)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(isVisible()) {
                            Glide.with(requireContext()).load(uri).into(ivUserImage);
                        }
                    }
                });

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("iw")); // For the date
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("iw")); // For the time

        ((TextView) view.findViewById(R.id.tvDate)).setText(dateFormat.format(currentDate));
        ((TextView) view.findViewById(R.id.tvTime)).setText(timeFormat.format(currentDate));

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        ((TextView) view.findViewById(R.id.tvDayStatus)).setText(
                currentHour < 12 && currentHour >= 5 ? "בוקר טוב" : currentHour < 18 && currentHour >= 12 ? "צהריים טובים" : "ערב טוב"
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUi();
        updateCaseList();
    }
}