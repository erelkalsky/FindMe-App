package com.example.findme.home.settings.editUser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.ui.EditTextListener;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {
    ErrorEditText editText;
    LoadingButton btnSave;

    final Map<String, String> TRANSFORM_HEBREW = new HashMap<String, String>() {{
        put("phone", "טלפון");
        put("firstName", "שם פרטי");
        put("lastName", "שם משפחה");
    }};

    final Map<String, String> TRANSFORM_KEYBOARD = new HashMap<String, String>() {{
        put("phone", "phone");
        put("firstName", "hebrew");
        put("lastName", "hebrew");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        String toUpdate = getIntent().getStringExtra("edit");

        btnSave = findViewById(R.id.btnSave);

        editText = findViewById(R.id.errorEditText);
        editText.setHint(TRANSFORM_HEBREW.get(toUpdate));

        String keyboard = TRANSFORM_KEYBOARD.get(toUpdate);
        if(keyboard.equals("hebrew")) {
            editText.setKeyboard(keyboard, 15);
        } else if(keyboard.equals("phone")) {
            editText.setKeyboard(keyboard, 10);
        } else {
            editText.setKeyboard(keyboard, 0);
        }

        switch (toUpdate) {
            case "phone":
                editText.setText(Database.user.getPhone());
                editText.addTextChangedListener(EditTextListener.phone(editText, btnSave));
                editText.addTextChangedListener(EditTextListener.equalToBefore(Database.user.getPhone(), btnSave));
                break;
            case "firstName":
                editText.setText(Database.user.getFirstName());
                editText.addTextChangedListener(EditTextListener.firstName(editText, btnSave));
                editText.addTextChangedListener(EditTextListener.equalToBefore(Database.user.getFirstName(), btnSave));
                break;
            case "lastName":
                editText.setText(Database.user.getLastName());
                editText.addTextChangedListener(EditTextListener.lastName(editText, btnSave));
                editText.addTextChangedListener(EditTextListener.equalToBefore(Database.user.getLastName(), btnSave));
                break;
        }


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updated = editText.getText().toString();

                if (btnSave.isInProgress()) {
                    return;
                }

                editText.removeError();
                btnSave.start();

                if(toUpdate.equals("phone")) {
                    Database.isPhoneExist(updated, new Database.IsPhoneExistListener() {
                        @Override
                        public void onPhoneExist() {
                            editText.setError("הטלפון שאתה רוצה לשנות אליו תפוס, נסה טלפון אחר.");
                            btnSave.stop();
                        }

                        @Override
                        public void onPhoneNotExist() {
                            updateUser(toUpdate, updated);
                        }

                        @Override
                        public void onFailure(String errorTitle, String errorMessage, String buttonText) {
                            new ErrorDialog(
                                    EditUserActivity.this,
                                    errorTitle,
                                    errorMessage,
                                    buttonText
                            ).show();
                            btnSave.stop();
                        }

                    });
                } else {
                    updateUser(toUpdate, updated);
                }
            }
        });
    }

    private void updateUser(String toUpdate, String updated) {
        if(!Database.isNetworkConnected(EditUserActivity.this)) {
            new ErrorDialog(
                    EditUserActivity.this,
                    "בעיה ברשת",
                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                    "חזור"
            ).show();

            btnSave.stop();
            return;
        }

        Database.updateUser(toUpdate, updated, new Database.UpdateUserListener() {
            @Override
            public void onUpdateUserSuccess() {
                btnSave.stop();
                finish();
            }

            @Override
            public void onUpdateUserFailure() {
                btnSave.stop();

                ErrorDialog errorDialog = new ErrorDialog(
                        EditUserActivity.this,
                        "עידכון לא צלח",
                        "מסיבה לא ידועה העדכון של ה"+ TRANSFORM_HEBREW.get(toUpdate) +" שלך לא צלח. נסה שוב בשביל לעדכן אותו.",
                        "נסה שוב"
                );

                errorDialog.show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}