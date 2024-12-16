package com.example.findme.classes.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.example.findme.R;

public class ConfirmDialog extends Dialog {
    private TextView tvTitle, tvDescription;
    private TextView btnOk, btnCancel;

    public ConfirmDialog(Context context, String title, String description, String buttonOkText, String buttonCancelText, ConfirmClickListener listener) {
        super(context);

        setContentView(R.layout.dialog_confirm);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);


        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        tvDescription = findViewById(R.id.tvDescription);
        tvDescription.setText(description);

        btnOk = findViewById(R.id.btnOk);
        btnOk.setText(buttonOkText);
        btnOk.setOnClickListener(v -> {
            listener.onOkClicked();
            dismiss();
        });

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setText(buttonCancelText);
        btnCancel.setOnClickListener(v -> {
            listener.onCancelClick();
            dismiss();
        });
    }
}
