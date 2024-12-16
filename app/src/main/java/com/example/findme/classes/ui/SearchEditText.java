package com.example.findme.classes.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findme.R;

public class SearchEditText extends LinearLayout {
    private TextView btnCancel;
    private EditText editText;

    public SearchEditText(Context context) {
        super(context);
        init(null);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.search_edit_text, this, true);

        btnCancel = findViewById(R.id.btnCancel);
        editText = findViewById(R.id.editText);

        btnCancel.setOnClickListener(v -> {
            editText.setText("");
            hideKeyboard();
            showCancelButton(false);
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showCancelButton(s.length() >= 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showCancelButton(true); //keyboard is open
                } else {
                    if(editText.getText().toString().isEmpty()) {
                        showCancelButton(false); //keyboard is close
                    }
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Clear focus from the EditText
                    editText.clearFocus();

                    // Hide the soft keyboard
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void showCancelButton(boolean show) {
        btnCancel.setVisibility(show ? VISIBLE : GONE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }

        editText.clearFocus();
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public String getText() {
        return editText.getText().toString();
    }
}
