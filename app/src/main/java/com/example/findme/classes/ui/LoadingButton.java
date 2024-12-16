package com.example.findme.classes.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.findme.R;

public class LoadingButton extends LinearLayout {
    private TextView textView;
    private ProgressBar progressBar;
    private boolean inProgress = false;
    private boolean locked = false;

    public LoadingButton(Context context) {
        super(context);
        init(null);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.loading_button, this, true);

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        setDisabled();

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingButton);
            String buttonText = a.getString(R.styleable.LoadingButton_buttonText);
            textView.setText(buttonText);
        }
    }

    public void start() {
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        inProgress = true;
    }

    public void stop() {
        textView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        inProgress = false;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setEnabled() {
        this.setEnabled(true);
        findViewById(R.id.linearLayout).setAlpha(1f);
    }

    public void setDisabled() {
        this.setEnabled(false);
        findViewById(R.id.linearLayout).setAlpha(0.5f);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setButtonText(String buttonText) {
        textView.setText(buttonText);
    }
}
