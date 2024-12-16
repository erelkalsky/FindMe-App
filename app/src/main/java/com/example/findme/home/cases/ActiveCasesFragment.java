package com.example.findme.home.cases;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.CaseAdapter;
import com.example.findme.classes.ui.SearchEditText;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;


import java.util.ArrayList;
import java.util.List;

public class ActiveCasesFragment extends Fragment implements Database.CollectionUpdateListener {
    View view;
    List<Case> caseList = new ArrayList<>();
    List<String> caseListId = new ArrayList<>();
    CaseAdapter adapter;
    SearchEditText etSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_active_cases, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCases);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CaseAdapter(caseList, caseListId, requireContext());
        recyclerView.setAdapter(adapter);

        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.setFilter(editable.toString());
                adapter.notifyDataSetChanged();
            }
        });

        Database.listenForCollectionUpdates("cases", this);

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

                            if(aCase.isActive()) {
                                caseList.add(aCase);
                                caseListId.add(document.getId());
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onCollectionUpdated() {
        updateCaseList();
    }
}