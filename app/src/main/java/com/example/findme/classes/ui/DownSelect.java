package com.example.findme.classes.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.findme.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DownSelect extends LinearLayout {
    Spinner spinner;
    TextView tvError;

    public DownSelect(Context context) {
        super(context);
        init(null);
    }

    public DownSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DownSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.down_select, this, true);
        spinner = findViewById(R.id.spinner);
        tvError = findViewById(R.id.tvError);

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DownSelect);
            String strList = a.getString(R.styleable.DownSelect_items);

            List<String> list = new ArrayList<>();
            list.add(a.getString(R.styleable.DownSelect_start));
            list.addAll(new ArrayList<>(Arrays.asList(strList.split(",\\s*"))));

            spinner.setAdapter(new SelectAdapter(list));
            spinner.setSelection(0, true);
        }
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener itemSelectedListener) {
        spinner.setOnItemSelectedListener(itemSelectedListener);
    }

    public int getItem() {
        return (int) spinner.getSelectedItem();
    }

    public void setError(String error) {
        spinner.setBackground(getContext().getDrawable(R.drawable.style_edit_text_error));
        tvError.setText(error);
        tvError.setVisibility(View.VISIBLE);
    }

    public void removeError() {
        spinner.setBackground(getContext().getDrawable(R.drawable.style_edit_text));
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    public void setSelection(int selection) {
        spinner.setSelection(selection);
    }

    private class SelectAdapter extends BaseAdapter {
        List<String> list;

        public SelectAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rootView = LayoutInflater.from(getContext()).inflate(R.layout.down_select_adapter, viewGroup, false);

            TextView t = rootView.findViewById(R.id.textView);
            t.setText(list.get(i).toString());

            if(i == 0) {
                t.setTextColor(getResources().getColor(R.color.dark_gray));
            }

            return rootView;
        }

        @Override
        public boolean isEnabled(int position) {
            // Disable the first item in the Spinner
            return position != 0;
        }
    }
}
