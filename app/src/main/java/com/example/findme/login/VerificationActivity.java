package com.example.findme.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.findme.R;
import com.example.findme.classes.PasswordGenerator;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.api.SendSms;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.users.User;
import com.example.findme.home.cases.aCase.CaseUsersInCaseActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends AppCompatActivity {
    EditText code1, code2, code3, code4, code5, code6;
    LoadingButton btnResetPassword;
    boolean code1Enabled = false, code2Enabled = false,
            code3Enabled = false, code4Enabled = false,
            code5Enabled = false, code6Enabled = false;
    long secondsRemaining = -1;
    String verificationCode, userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        verificationCode = getIntent().getStringExtra("verificationCode");
        userPhone = getIntent().getStringExtra("userPhone");

        if(verificationCode == null || userPhone == null) {
            finish();
        }

        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        code5 = findViewById(R.id.code5);
        code6 = findViewById(R.id.code6);

        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String typedVerCode =
                        code1.getText().toString() +
                        code2.getText().toString() +
                        code3.getText().toString() +
                        code4.getText().toString() +
                        code5.getText().toString() +
                        code6.getText().toString();

                if(btnResetPassword.isInProgress()) {
                    return;
                }

                btnResetPassword.start();
                resetPassword(typedVerCode);
            }
        });

        TextView tvCountDown = findViewById(R.id.tvCountDown);
        CountDownTimer countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsRemaining = millisUntilFinished / 1000;
                tvCountDown.setText("זמן שנותר: " + (secondsRemaining + 1) + " שניות");
            }

            @Override
            public void onFinish() {
                if (!btnResetPassword.isInProgress()) {
                    finish();
                }
            }
        };
        countDownTimer.start();

        setScrollViewFit();
        setEditTextListeners();
    }

    private void resetPassword(String typedVerCode) {
        if(typedVerCode.equals(verificationCode)) {
            FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("phone", userPhone)
                    .get(Source.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                User user = querySnapshot.getDocuments().get(0).toObject(User.class);
                                String userId = querySnapshot.getDocuments().get(0).getId();

                                if(user == null || userId == null) {
                                    finish();
                                }

                                String newPassword = PasswordGenerator.generatePassword();

                                final Map<String, Object> updated = new HashMap() {{
                                    put("firstLogin", true);
                                    put("password", newPassword);
                                }};

                                if(!Database.isNetworkConnected(VerificationActivity.this)) {
                                    new ErrorDialog(
                                            VerificationActivity.this,
                                            "בעיה ברשת",
                                            "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה להתחבר שוב.",
                                            "חזור"
                                    ).show();

                                    return;
                                }

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .update(updated).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                String body = "שלום " + user.getFirstName() + " " + user.getLastName() + "הסיסמה החדשה שלך באפליקציית איתור נעדרים היא: " + newPassword;
                                                new SendSms(user.getPhone(), body).execute();

                                                ErrorDialog errorDialog = new ErrorDialog(
                                                        VerificationActivity.this,
                                                        "סיסמה אופסה",
                                                        "משתמש יקר, הסיסמה שלך אופסה ונשלחה לך לטלפון הודעה עם הסיסמה החדשה.",
                                                        "סיום"
                                                );

                                                errorDialog.setOnDismissListener(v -> finish());
                                                errorDialog.show();

                                                btnResetPassword.stop();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                ErrorDialog errorDialog = new ErrorDialog(
                                                        VerificationActivity.this,
                                                        "שגיאה לא ידועה",
                                                        "משתמש יקר, הסיסמה לא אופסה עקב שגיאה לא ידועה יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                        "סגור"
                                                );

                                                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialogInterface) {
                                                        if(secondsRemaining == 0) {
                                                            finish();
                                                        }
                                                    }
                                                });

                                                errorDialog.show();
                                                btnResetPassword.stop();
                                            }
                                        });
                            }
                        }
                    });
        } else {
            ErrorDialog errorDialog = new ErrorDialog(
                    VerificationActivity.this,
                    "סיסמה שגויה",
                    "משתמש יקר, הסיסמה שהזנת שגויה. יש לנסות שוב.",
                    "נסה שוב"
            );

            errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    if(secondsRemaining == 0) {
                        finish();
                    }
                }
            });

            errorDialog.show();
            btnResetPassword.stop();
        }
    }

    private void setEditTextListeners() {
        code1.addTextChangedListener(setTextWatcher(code2));
        code2.addTextChangedListener(setTextWatcher(code3));
        code3.addTextChangedListener(setTextWatcher(code4));
        code4.addTextChangedListener(setTextWatcher(code5));
        code5.addTextChangedListener(setTextWatcher(code6));
        code6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(!s.toString().trim().isEmpty()) {
                    code6.clearFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                code6Enabled = editable.toString().isEmpty() ? false : true;
                updateButton();
            }
        });


        code2.setOnKeyListener(setOnKeyListener(code2, code1));
        code3.setOnKeyListener(setOnKeyListener(code3, code2));
        code4.setOnKeyListener(setOnKeyListener(code4, code3));
        code5.setOnKeyListener(setOnKeyListener(code5, code4));
        code6.setOnKeyListener(setOnKeyListener(code6, code5));
    }

    private TextWatcher setTextWatcher(EditText next) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(!s.toString().trim().isEmpty()) {
                    next.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                boolean b = !editable.toString().isEmpty();
                if(next == code2) {
                    code1Enabled = b;
                } else if(next == code3) {
                    code2Enabled = b;
                } else if(next == code4) {
                    code3Enabled = b;
                } else if(next == code5) {
                    code4Enabled = b;
                } else if(next == code6) {
                    code5Enabled = b;
                }

                updateButton();
            }
        };
    }

    private void updateButton() {
        if(code1Enabled && code2Enabled && code3Enabled && code4Enabled && code5Enabled && code6Enabled) {
            btnResetPassword.setEnabled();
        } else {
            btnResetPassword.setDisabled();
        }
    }

    private View.OnKeyListener setOnKeyListener(EditText current, EditText before) {
        return new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if(current.getText().toString().isEmpty()) {
                        before.requestFocus();
                    }

                    return false;
                }
                return false;
            }
        };
    }


    private void setScrollViewFit() {
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = scrollView.getRootView().getHeight() - scrollView.getHeight();
                if (heightDiff > 100) {
                    scrollView.scrollTo(0, btnResetPassword.getBottom());
                } else {
                    scrollView.scrollTo(0, 0);
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