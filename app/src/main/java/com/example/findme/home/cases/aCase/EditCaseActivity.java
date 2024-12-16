package com.example.findme.home.cases.aCase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.EditTextListener;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;
import com.google.firebase.firestore.ListenerRegistration;

public class EditCaseActivity extends AppCompatActivity implements Database.CaseUpdateListener {
    Case aCase;
    String caseId;
    ErrorEditText editText;
    LoadingButton btnSave;
    String fieldName, fieldNameHebrew, value, keyboard, toUpdate;
    ListenerRegistration caseListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_case);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        caseId = getIntent().getStringExtra("caseId");

        editText = findViewById(R.id.errorEditText);
        btnSave = findViewById(R.id.btnSave);

        fieldName = getIntent().getStringExtra("fieldName");
        fieldNameHebrew = getIntent().getStringExtra("fieldNameHebrew");
        value = getIntent().getStringExtra("value");
        keyboard = getIntent().getStringExtra("keyboard");
        toUpdate = getIntent().getStringExtra("toUpdate");

        editText.setHint(fieldNameHebrew);
        editText.setText(value);
        setKeyboard();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updated = editText.getText().toString();

                if (btnSave.isInProgress()) {
                    return;
                }

                editText.removeError();
                btnSave.start();

                if (fieldName.equals("caseId")) {
                    checkCaseId(updated);
                } else {
                    update(updated);
                }
            }
        });
    }

    private void setKeyboard() {
        switch (keyboard) {
            case "big":
                editText.setBigEditText();
                editText.addTextChangedListener(EditTextListener.bigText(value, btnSave));
                //changeFooterVisibility();

                break;
            case "number":
                if (fieldName.equals("id")) {
                    editText.setKeyboard(keyboard, 9);
                    editText.addTextChangedListener(EditTextListener.personId(editText, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));
                } else if (fieldName.equals("age")) {
                    editText.setKeyboard(keyboard, 3);
                    editText.addTextChangedListener(EditTextListener.validAge(value, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));
                } else if (fieldName.equals("caseId")) {
                    editText.setKeyboard(keyboard, 6);
                    editText.addTextChangedListener(EditTextListener.caseId(editText, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));
                } else if(fieldName.equals("height")) {
                    editText.setKeyboard(keyboard, 3);
                    editText.addTextChangedListener(EditTextListener.validHeight(value, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));

                    editText.setHint("גובה (סנטימטר)");
                }

                break;
            case "phone":
                editText.setKeyboard(keyboard, 10);
                editText.addTextChangedListener(EditTextListener.validPhone(editText, value, btnSave));
                editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));

                break;
            case "hebrew":
                editText.setKeyboard(keyboard, 15);

                if (fieldName.equals("firstName")) {
                    editText.addTextChangedListener(EditTextListener.firstName(editText, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));
                } else if (fieldName.equals("lastName")) {
                    editText.addTextChangedListener(EditTextListener.firstName(editText, btnSave));
                    editText.addTextChangedListener(EditTextListener.equalToBefore(value, btnSave));
                }

                break;
        }
    }

    private void checkCaseId(String updated) {
        Database.isCaseExist(updated, new Database.IsCaseExistListener() {
            @Override
            public void onCaseExist() {
                editText.setError("מספר תיק כבר קיים במערכת.");
                btnSave.stop();
            }

            @Override
            public void onCaseNotExist() {
                update(updated);
            }

            @Override
            public void onFailure(String errorTitle, String errorMessage, String buttonText) {
                new ErrorDialog(
                        EditCaseActivity.this,
                        errorTitle,
                        errorMessage,
                        buttonText
                ).show();
                btnSave.stop();
            }
        });
    }

    private void update(String updated) {
        if(!Database.isNetworkConnected(this)) {
            new ErrorDialog(
                    EditCaseActivity.this,
                    "בעיה ברשת",
                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                    "חזור"
            ).show();

            btnSave.stop();
            return;
        }

        Database.updateCase(caseId, toUpdate, updated.isEmpty() ? null : updated, new Database.UpdateCaseListener() {
            @Override
            public void onUpdateCaseSuccess() {
                btnSave.stop();
                finish();
            }

            @Override
            public void onUpdateCaseFailure() {
                btnSave.stop();

                new ErrorDialog(
                        EditCaseActivity.this,
                        "עידכון לא צלח",
                        "מסיבה לא ידועה העדכון של ה"+ fieldNameHebrew +" שלך לא צלח. נסה שוב בשביל לעדכן אותו.",
                        "נסה שוב"
                ).show();
            }
        });
    }

    private void setEditable(boolean editable) {
        editText.setAvailable(editable);
        btnSave.setVisibility(editable ? View.VISIBLE : View.GONE);
        findViewById(R.id.footerLayout).setVisibility(editable ? View.VISIBLE : View.GONE);

        if(!editable) {
            ErrorDialog errorDialog = new ErrorDialog(
                    EditCaseActivity.this,
                    "אין גישה",
                    "משתמש יקר, ככל הנראה התיק נסגר או שהוסרת מפעילות בתיק.",
                    "סגירה"
            );

            if(editText.hasFocus()) {
                errorDialog.setOnDismissListener(v -> finish());
                errorDialog.show();
            }
        }
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;

        setEditable(aCase.isActive() && aCase.isUserInCase(Database.userId));
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

    @Override
    protected void onStart() {
        super.onStart();
        caseListenerRegistration = Database.listenForCaseUpdates(this, caseId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(caseListenerRegistration != null) {
            caseListenerRegistration.remove();
        }
    }
}