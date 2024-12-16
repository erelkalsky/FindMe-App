package com.example.findme.classes.cases;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class CasesPagerAdapter extends FragmentPagerAdapter {
    Fragment[] fragments;
    String[] titles;

    public CasesPagerAdapter(FragmentManager fm, Fragment[] fragments, String[] titles) {
        super(fm);

        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
