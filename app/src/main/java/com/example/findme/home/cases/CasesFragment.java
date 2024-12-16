package com.example.findme.home.cases;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.CasesPagerAdapter;
import com.example.findme.home.cases.createCase.CreateCaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class CasesFragment extends Fragment {
    View view;

    final Fragment[] fragments = new Fragment[] {
            new ActiveCasesFragment(),
            new HistoricalCasesFragment()
    };

    final String[] titles = new String[] {
            "תיקים פעילים",
            "היסטוריית תיקים"
    };

    FloatingActionButton btnCreateCase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_cases, container, false);

        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new CasesPagerAdapter(getChildFragmentManager(), fragments, titles));

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        btnCreateCase = view.findViewById(R.id.btnCreateCase);
        btnCreateCase.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), CreateCaseActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        updateUi();

        return view;
    }

    public void updateUi() {
        if(Database.user != null && Database.user.getRole() == 1) {
            btnCreateCase.setVisibility(View.GONE);
        } else {
            btnCreateCase.setVisibility(View.VISIBLE);
        }
    }
}