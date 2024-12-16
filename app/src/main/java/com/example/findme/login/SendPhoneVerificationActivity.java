package com.example.findme.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.example.findme.R;
import com.example.findme.classes.PasswordGenerator;
import com.example.findme.classes.api.SendSms;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class SendPhoneVerificationActivity extends AppCompatActivity {
    ErrorEditText etPhone;
    LoadingButton btnSendsSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_phone_verification);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        etPhone = findViewById(R.id.etPhone);
        etPhone.addTextChangedListener(textWatcherPhone());

        btnSendsSms = findViewById(R.id.btnSendsSms);
        btnSendsSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = etPhone.getText().toString();

                if(btnSendsSms.isInProgress()) {
                    return;
                }

                btnSendsSms.start();

                FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("phone", phone)
                        .get(Source.SERVER)
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    User user = querySnapshot.getDocuments().get(0).toObject(User.class);

                                    String genPassword = PasswordGenerator.generateNumericPassword(6);
                                    String body = "קוד האימות לאיפוס הסיסמה שלך הוא: " + genPassword;
                                    new SendSms(user.getPhone(), body).execute();

                                    startActivity(
                                            new Intent(SendPhoneVerificationActivity.this, VerificationActivity.class)
                                                    .putExtra("verificationCode", genPassword)
                                                    .putExtra("userPhone", phone)
                                    );

                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                } else {
                                    etPhone.setError("משתמש לא קיים עם מספר טלפון זה.");
                                    btnSendsSms.stop();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                new ErrorDialog(
                                        SendPhoneVerificationActivity.this,
                                        "סיבה לא ידועה",
                                        "אירעה שגיאה לא ידועה יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                        "סגור"
                                ).show();

                                btnSendsSms.stop();
                            }
                        });
            }
        });

        setScrollViewFit();
    }

    private void setScrollViewFit() {
        ScrollView scrollView = findViewById(R.id.scrollView);

        // Add a view tree observer to listen for layout changes
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Check if the root view height changes
                int heightDiff = scrollView.getRootView().getHeight() - scrollView.getHeight();
                if (heightDiff > 100) { // Adjust this threshold according to your needs
                    // Keyboard is visible, scroll to the focused input field
                    scrollView.scrollTo(0, btnSendsSms.getBottom()); // Adjust to the desired field
                } else {
                    // Keyboard is hidden, scroll to the top of the layout
                    scrollView.scrollTo(0, 0);
                }
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
                    btnSendsSms.setDisabled();
                    etPhone.removeError();
                } else if (isValidPhone(phone)) {
                    etPhone.removeError();
                    btnSendsSms.setEnabled();
                } else {
                    btnSendsSms.setDisabled();
                    etPhone.setError("מספר טלפון אינו תקין.");
                }
            }
        };
    }

    private boolean isValidPhone(String phone) {
        String regex = "05[0-9]{8}";
        return phone.matches(regex);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}