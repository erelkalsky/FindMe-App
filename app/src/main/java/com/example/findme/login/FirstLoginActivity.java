package com.example.findme.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.api.SendSms;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.users.User;
import com.example.findme.home.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirstLoginActivity extends AppCompatActivity {
    ErrorEditText etNewPassword, etConfirmPassword;
    LoadingButton btnSave;
    boolean newPasswordEnabled = false, confirmPasswordEnabled = false;
    final String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*])(?=.*[a-zA-Z0-9@#$%^&+=!]).{6,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        String userId = getIntent().getStringExtra("userId");

        btnSave = findViewById(R.id.btnSave);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        setEditTextListeners();
        setScrollViewFit();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = etNewPassword.getText().toString();

                if(btnSave.isInProgress()) {
                    return;
                }

                btnSave.start();

                final Map<String, Object> updated = new HashMap() {{
                    put("firstLogin", false);
                    put("password", newPassword);
                }};

                if(!Database.isNetworkConnected(FirstLoginActivity.this)) {
                    new ErrorDialog(
                            FirstLoginActivity.this,
                            "בעיה ברשת",
                            "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה להתחבר שוב.",
                            "חזור"
                    ).show();

                    btnSave.stop();
                    
                    return;
                }

                FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .update(updated).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener(doc -> {
                                    User user = doc.toObject(User.class);
                                    if(user != null) {
                                        String body = "שלום " + user.getFirstName() + " " + user.getLastName() + ", סיסמתך עודכנה בהצלחה.";
                                        new SendSms(user.getPhone(), body).execute();
                                    }
                                });

                                Database.login(FirstLoginActivity.this, userId, new Database.LoginListener() {
                                    @Override
                                    public void onLoginSuccess() {
                                        startActivity(new Intent(FirstLoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

                                        btnSave.stop();
                                        finish();
                                    }

                                    @Override
                                    public void onFirstLogin(String userId) {
                                        //not in use with id login

                                    }

                                    @Override
                                    public void onUserLocked() {
                                        //not in use with id login
                                    }

                                    @Override
                                    public void onUserNotFound() {
                                        ErrorDialog errorDialog = new ErrorDialog(
                                                FirstLoginActivity.this,
                                                "משתמש לא נמצא",
                                                "ככל הנראה המשתמש שאתה מנסה לעדכן את סיסמתו נמחק. אנא התחבר למשתמש אחר או נסה להתחבר מחדש.",
                                                "סיום"
                                        );

                                        errorDialog.setOnDismissListener(v -> {
                                            startActivity(new Intent(FirstLoginActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                            finish();
                                        });

                                        errorDialog.show();
                                        btnSave.stop();
                                    }

                                    @Override
                                    public void onLoginFailure(String errorTitle, String errorMessage, String buttonText) {
                                        ErrorDialog errorDialog = new ErrorDialog(
                                                FirstLoginActivity.this,
                                                errorTitle,
                                                errorMessage,
                                                buttonText
                                        );

                                        errorDialog.setOnDismissListener(v -> {
                                            startActivity(new Intent(FirstLoginActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                            finish();
                                        });

                                        errorDialog.show();
                                        btnSave.stop();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                new ErrorDialog(
                                        FirstLoginActivity.this,
                                        "סיבה לא ידועה",
                                        "אירעה שגיאה לא ידועה במהלך עדכון הסיסמה. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                        "חזור"
                                ).show();

                                btnSave.stop();
                            }
                        });
            }
        });
    }

    private void setEditTextListeners() {
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

                if(newPasswordEnabled && confirmPasswordEnabled && newPassword.equals(etConfirmPassword.getText().toString())) {
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

                if(newPasswordEnabled && confirmPasswordEnabled) {
                    btnSave.setEnabled();
                } else {
                    btnSave.setDisabled();
                }
            }
        });
    }

    private void setScrollViewFit() { //scroll to fit view while keyboard visible
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = scrollView.getRootView().getHeight() - scrollView.getHeight();
                if (heightDiff > 100) {
                    scrollView.scrollTo(0, btnSave.getBottom());
                } else {
                    scrollView.scrollTo(0, 0);
                }
            }
        });
    }
}