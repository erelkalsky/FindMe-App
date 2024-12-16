package com.example.findme.classes.ui;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findme.R;


public class ErrorEditText extends LinearLayout {
    EditText editText;
    TextView tvError, tvTitle, tvMaxChars;
    final InputFilter hebrewFilter = (source, start, end, dest, dstart, dend) ->
            source.subSequence(start, end).toString().replaceAll("[^א-ת\\s]", "");

    public ErrorEditText(Context context) {
        super(context);
        init(null);
    }

    public ErrorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ErrorEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setError(String error) {
        editText.setBackground(getContext().getDrawable(R.drawable.style_edit_text_error));
        tvError.setText(error);
        tvError.setVisibility(View.VISIBLE);
    }

    public void removeError() {
        editText.setBackground(getContext().getDrawable(R.drawable.style_edit_text));
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.error_edit_text, this, true);

        editText = findViewById(R.id.editText);
        tvError = findViewById(R.id.tvError);
        tvTitle = findViewById(R.id.tvTitle);
        tvMaxChars = findViewById(R.id.tvMaxChars);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ErrorEditText);

            String hint = a.getString(R.styleable.ErrorEditText_hint);
            if(hint != null) {
                setHint(hint);
            }

            if(!a.getBoolean(R.styleable.ErrorEditText_topHint, true)) {
                tvTitle.setVisibility(GONE);
            }

            int maxChars = a.getInteger(R.styleable.ErrorEditText_maxChars, 0);

            String keyboard = a.getString(R.styleable.ErrorEditText_keyboard);
            setKeyboard(keyboard, maxChars);

            if(a.getBoolean(R.styleable.ErrorEditText_bigEditText, false)) {
                LinearLayout.LayoutParams params = (LayoutParams) editText.getLayoutParams();
                params.height = 0;
                params.weight = 1;
                editText.setGravity(Gravity.RIGHT | Gravity.TOP);
                editText.setPadding(20, 10, 20, 10);
                editText.setLayoutParams(params);
                editText.setScrollContainer(true);
            }
        }
    }

    public void setHint(String hint) {
        editText.setHint(hint);
        tvTitle.setText("הכנס " + hint);
    }

    public void setBigEditText() {
        LinearLayout.LayoutParams params = (LayoutParams) editText.getLayoutParams();
        params.height = 0;
        params.weight = 1;
        editText.setGravity(Gravity.RIGHT | Gravity.TOP);
        editText.setPadding(20, 10, 20, 10);
        editText.setLayoutParams(params);
        editText.setScrollContainer(true);

        LinearLayout.LayoutParams params2 = (LayoutParams) getLayoutParams();
        params2.height = 0;
        params2.weight = 1;
        setLayoutParams(params2);
    }

    public void setMaxChars(int maxChars) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tvMaxChars.setText(editable.length() + "/" + maxChars);
            }
        });


        tvMaxChars.setText(editText.getText().toString().length() + "/" + maxChars);
        tvMaxChars.setVisibility(View.VISIBLE);
    }

    public void setKeyboard(String keyboard, int maxChars) {
        switch (keyboard) {
            case "text":
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                }

                break;
            case "email":
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                }

                break;
            case "phone":
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                }

                break;
            case "password":
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                setIconVisible();
                setViewPasswordListener();

                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                }

                break;
            case "hebrew":
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ hebrewFilter, new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                } else {
                    editText.setFilters(new InputFilter[]{ hebrewFilter });
                }

                break;
            case "number":
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                if(maxChars > 0) {
                    editText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(maxChars) });
                    setMaxChars(maxChars);
                }

                break;
        }
    }

    private void setIconVisible() {
        Drawable drawable = getResources().getDrawable(R.drawable.icon_visibility);
        drawable.setTint(getResources().getColor(R.color.darkest_gray));

        editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    private void setIconVisibleOff() {
        Drawable drawable = getResources().getDrawable(R.drawable.icon_visibility_off);
        drawable.setTint(getResources().getColor(R.color.darkest_gray));

        editText.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    private void setViewPasswordListener() {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Calculate the position of the touch event relative to the EditText
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    // Get the bounds of the left compound drawable
                    Drawable[] drawables = editText.getCompoundDrawables();
                    Drawable drawableLeft = drawables[0]; // Assuming the drawable is on the left
                    if (drawableLeft != null) {
                        int drawableWidth = drawableLeft.getBounds().width();
                        int drawableHeight = drawableLeft.getBounds().height();

                        // Calculate the bounds of the left drawable //plus 2 it me for additional space o click
                        int drawableLeftX = editText.getLeft() + editText.getPaddingLeft() + 5;
                        int drawableRightX = drawableLeftX + drawableWidth + 5;
                        int drawableTopY = editText.getTop() + editText.getPaddingTop() + 5;
                        int drawableBottomY = drawableTopY + drawableHeight + 5;

                        // Check if the touch event is within the bounds of the left drawable
                        if (x >= drawableLeftX && x <= drawableRightX && y >= drawableTopY && y <= drawableBottomY) {
                            // Left drawable clicked
                            // Perform your action here
                            // For example, log that the left drawable is clicked

                            //Toast.makeText(getContext(), "Left drawable is clicked", Toast.LENGTH_SHORT).show();

                            //int selectionStart = editText.getSelectionStart();
                           // int selectionEnd = editText.getSelectionEnd();

                            if(editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                setIconVisibleOff();
                            } else if(editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                setIconVisible();
                            }

                            //editText.setSelection(selectionStart, selectionEnd);

                            return true; // Consume the touch event
                        }
                    }
                }
                return false; // Let other touch listeners handle the touch event
            }
        });
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        editText.setText(text);
    }

    public void setAvailable(boolean available) {
        editText.setEnabled(available);
    }
}
