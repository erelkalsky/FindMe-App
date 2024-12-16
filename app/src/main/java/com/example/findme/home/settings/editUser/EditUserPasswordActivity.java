package com.example.findme.home.settings.editUser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;

public class EditUserPasswordActivity extends AppCompatActivity {
    ErrorEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    LoadingButton btnSave;
    boolean currentPasswordEnabled = false, newPasswordEnabled = false, confirmPasswordEnabled = false;
    final String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*])(?=.*[a-zA-Z0-9@#$%^&+=!]).{6,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_password);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        btnSave = findViewById(R.id.btnSave);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        setEditTextListeners();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = etNewPassword.getText().toString();

                if (btnSave.isInProgress()) {
                    return;
                }

                etCurrentPassword.removeError();
                etNewPassword.removeError();
                etConfirmPassword.removeError();
                btnSave.start();

                if(!etCurrentPassword.getText().toString().equals(Database.user.getPassword())) {
                    etCurrentPassword.setError("הסיסמה שהזנת היא לא הסיסמה הנוכחית של המשתמש.");
                    btnSave.stop();
                } else {
                    updateUser(newPassword);
                }
            }
        });
    }

    private void updateUser(String newPassword) {
        if(!Database.isNetworkConnected(EditUserPasswordActivity.this)) {
            new ErrorDialog(
                    EditUserPasswordActivity.this,
                    "בעיה ברשת",
                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך שוב.",
                    "חזור"
            ).show();

            btnSave.stop();
            return;
        }

        Database.updateUser("password", newPassword, new Database.UpdateUserListener() {
            @Override
            public void onUpdateUserSuccess() {
                btnSave.stop();

                ErrorDialog errorDialog = new ErrorDialog(
                        EditUserPasswordActivity.this,
                        "עידכון צלח",
                        "משתמש יקר, הסיסמה של עודכנה בהצלחה.",
                        "סיום"
                );

                errorDialog.setOnDismissListener(v -> finish());
                errorDialog.show();
            }

            @Override
            public void onUpdateUserFailure() {
                btnSave.stop();
                new ErrorDialog(
                        EditUserPasswordActivity.this,
                        "עידכון לא צלח",
                        "מסיבה לא ידועה העדכון של הסיסמה  שלך לא צלח. נסה שוב בשביל לעדכן אותו.",
                        "נסה שוב"
                ).show();
            }
        });
    }

    private void setEditTextListeners() {
        etCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String currentPassword = editable.toString();

                if(currentPassword.isEmpty()) {
                    currentPasswordEnabled = false;
                    etCurrentPassword.removeError();

                } else if (currentPassword.length() < 2) {
                    currentPasswordEnabled = false;
                    etCurrentPassword.setError("סיסמה לא תקינה");
                } else {
                    currentPasswordEnabled = true;
                    etCurrentPassword.removeError();
                }

                if(currentPasswordEnabled && newPasswordEnabled && confirmPasswordEnabled && etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
                    btnSave.setEnabled();
                } else {
                    btnSave.setDisabled();
                }
            }
        });

        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newPassword = editable.toString();

                if(newPassword.isEmpty()) {
                    newPasswordEnabled = false;
                    etNewPassword.removeError();

                } else if (newPassword.matches(passwordRegex)) {
                    newPasswordEnabled = true;
                    etNewPassword.removeError();
                } else {
                    newPasswordEnabled = false;
                    etNewPassword.setError("הסיסמה חייבת להכיל לפחות שש תווים, אות גדולה, אות קטנה, מספר ותו מיוחד.");
                }

                if(currentPasswordEnabled && newPasswordEnabled && confirmPasswordEnabled && newPassword.equals(etConfirmPassword.getText().toString())) {
                    btnSave.setEnabled();
                } else {
                    btnSave.setDisabled();
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String confirmPassword = editable.toString();

                if(confirmPassword.isEmpty()) {
                    confirmPasswordEnabled = false;
                    etConfirmPassword.removeError();

                } else if (confirmPassword.equals(etNewPassword.getText().toString())) {
                    confirmPasswordEnabled = true;
                    etConfirmPassword.removeError();
                } else {
                    confirmPasswordEnabled = false;
                    etConfirmPassword.setError("אישור סיסמה חייב להיות זהה לסיסמה החדשה.");
                }

                if(currentPasswordEnabled && newPasswordEnabled && confirmPasswordEnabled) {
                    btnSave.setEnabled();
                } else {
                    btnSave.setDisabled();
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}