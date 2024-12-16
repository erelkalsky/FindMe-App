package com.example.findme.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.dialogs.LoadingDialog;
import com.example.findme.home.MainActivity;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class LoginActivity extends AppCompatActivity {
    ErrorEditText etPhone, etPassword;
    LoadingButton btnLogin;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etPhone.addTextChangedListener(textWatcherPhone());

        etPassword = findViewById(R.id.etPassword);
        etPassword.addTextChangedListener(textWatcherPassword());

        btnLogin = findViewById(R.id.btnLogin);

        SharedPreferences sharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("currentUser", null);

        if(currentUserId != null) {
            connectUser(currentUserId);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnLogin.isInProgress()) {
                    return;
                }

                login();
            }
        });

        findViewById(R.id.btnForgotPassword).setOnClickListener(v -> {
            startActivity(new Intent(this, SendPhoneVerificationActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        setScrollViewFit();
    }

    private void setScrollViewFit() { //scroll to fit view while keyboard visible
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = scrollView.getRootView().getHeight() - scrollView.getHeight();
                if (heightDiff > 100) {
                    scrollView.scrollTo(0, btnLogin.getBottom());
                } else {
                    scrollView.scrollTo(0, 0);
                }
            }
        });
    }

    private void login() {
        btnLogin.start();

        String phone = etPhone.getText().toString(),
                password = etPassword.getText().toString();

        resetFields();
        if(!checkFields(phone, password)) {
            return;
        }

        connectUser(phone, password);
    }

    private void resetFields() {
        etPhone.removeError();
        etPassword.removeError();
    }

    private boolean checkFields(String phone, String password) {
        return !phone.isEmpty() && !password.isEmpty();
    }

    private void connectUser(String phone, String password) {
        Database.login(this, phone, password, new Database.LoginListener() {
            @Override
            public void onLoginSuccess() {
                startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

                btnLogin.stop();
                finish();
            }

            @Override
            public void onFirstLogin(String userId) {
                startActivity(
                        new Intent(LoginActivity.this, FirstLoginActivity.class)
                                .putExtra("userId", userId)
                                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                );

                btnLogin.stop();
                finish();
            }

            @Override
            public void onUserLocked() {
                new ErrorDialog(
                        LoginActivity.this,
                        "המשתמש נחסם",
                        "לא תוכל להיכנס למערכת מכיוון שמשתמשך נחסם, פנה למנהלים להורדת החסימה.",
                        "סיום"
                ).show();

                btnLogin.stop();
            }

            @Override
            public void onUserNotFound() {
                etPhone.setError("טלפון או סיסמה לא נכונים.");

                etPassword.setError("טלפון או סיסמה לא נכונים.");
                btnLogin.stop();

                FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("phone", phone)
                        .get(Source.SERVER)
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                                    String userId = querySnapshot.getDocuments().get(0).getId();

                                    if(userId == null || (user.getLoginAttempts() == Integer.parseInt(getString(R.string.login_attempts)))) {
                                        return;
                                    }

                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(userId)
                                            .update("loginAttempts", user.getLoginAttempts() + 1);
                                }
                            }
                        });
            }

            @Override
            public void onLoginFailure(String errorTitle, String errorMessage, String buttonText) {
                new ErrorDialog(
                        LoginActivity.this,
                        errorTitle,
                        errorMessage,
                        buttonText
                ).show();

                btnLogin.stop();
            }
        });
    }

    private void connectUser(String userId) {
        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Database.login(this, userId, new Database.LoginListener() {
            @Override
            public void onLoginSuccess() {
                startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

                btnLogin.stop();
                loadingDialog.dismiss();
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
                new ErrorDialog(
                        LoginActivity.this,
                        "משתמש לא נמצא",
                        "ככל הנראה המשתמש שאתה מנסה להתחבר אליו נמחק. אנא התחבר למשתמש אחר או נסה להתחבר מחדש.",
                        "סיום"
                ).show();

                btnLogin.stop();
                loadingDialog.dismiss();
            }

            @Override
            public void onLoginFailure(String errorTitle, String errorMessage, String buttonText) {
                new ErrorDialog(
                        LoginActivity.this,
                        errorTitle,
                        errorMessage,
                        buttonText
                ).show();

                btnLogin.stop();
                loadingDialog.dismiss();
            }
        });
    }

    private TextWatcher textWatcherPhone() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = editable.toString();

                if(phone.isEmpty()) {
                    btnLogin.setDisabled();
                    etPhone.removeError();
                } else if (isValidPhone(phone)) {
                    etPhone.removeError();

                    if(isValidPassword(etPassword.getText().toString())) {
                        btnLogin.setEnabled();
                    }
                } else {
                    btnLogin.setDisabled();
                    etPhone.setError("מספר טלפון אינו תקין.");
                }
            }
        };
    }

    private TextWatcher textWatcherPassword() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String password = editable.toString();

                if(password.isEmpty()) {
                    btnLogin.setDisabled();
                    etPassword.removeError();
                } else if (isValidPassword(password)) {
                    etPassword.removeError();

                    if(isValidPhone(etPhone.getText().toString())) {
                        btnLogin.setEnabled();
                    }
                } else {
                    btnLogin.setDisabled();
                    etPassword.setError("סיסמה לא תקינה");
                }
            }
        };
    }

    private boolean isValidPhone(String phone) {
        String regex = "05[0-9]{8}";
        return phone.matches(regex);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 2;
    }
}