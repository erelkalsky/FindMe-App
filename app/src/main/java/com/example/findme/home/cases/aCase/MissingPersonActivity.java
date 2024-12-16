package com.example.findme.home.cases.aCase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.MissingPerson;

import java.lang.reflect.Field;
import java.util.Map;

public class MissingPersonActivity extends AppCompatActivity implements Database.CaseUpdateListener {
    Case aCase;
    String caseId;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missing_person);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");

        scrollView = findViewById(R.id.scrollView);
        updateScrollView();

        Database.listenForCaseUpdates(this, caseId);
    }

    private void updateScrollView() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (Map.Entry<String, String[]> entry: MissingPerson.fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldNameHebrew = entry.getValue()[0];
            String keyboard = entry.getValue()[1];

            View contentView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_missing_person, null);

            TextView title = contentView.findViewById(R.id.tvTitle),
                    description = contentView.findViewById(R.id.tvDescription);

            description.setEllipsize(TextUtils.TruncateAt.END);

            title.setText(fieldNameHebrew);

            try {
                Field field = MissingPerson.class.getDeclaredField(fieldName);

                field.setAccessible(true);
                Object value = field.get(aCase.getMissingPerson());
                field.setAccessible(false);

                if (value == null) {
                    description.setText("לא הוזן");
                } else {
                    description.setText(value.toString());
                }


                contentView.findViewById(R.id.btnMaps).setOnClickListener(v -> {
                    startActivity(
                            new Intent(MissingPersonActivity.this, EditCaseActivity.class)
                                    .putExtra("fieldName", fieldName)
                                    .putExtra("fieldNameHebrew", fieldNameHebrew)
                                    .putExtra("value", (String) value)
                                    .putExtra("keyboard", keyboard)
                                    .putExtra("caseId", caseId)
                                    .putExtra("toUpdate","missingPerson." + fieldName)
                    );

                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                });


            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }

            linearLayout.addView(contentView);
        }

        scrollView.removeAllViews();
        scrollView.addView(linearLayout);
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;

        ((TextView) findViewById(R.id.tvCaseId)).setText("מספר תיק: " + aCase.getCaseId());
        updateScrollView();
    }

    @Override
    public void onCaseDeleted() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}