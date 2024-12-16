package com.example.findme.classes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.example.findme.R;

public class ErrorDialog extends Dialog {
    private TextView tvTitle, tvDescription;
    private TextView btnCancel;

    public ErrorDialog(Context context, String title, String description, String buttonText) {
        super(context);

        setContentView(R.layout.dialog_error);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);

        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(description);

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setText(buttonText);
        btnCancel.setOnClickListener(view -> dismiss());
    }
}
