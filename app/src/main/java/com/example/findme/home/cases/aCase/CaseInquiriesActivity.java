package com.example.findme.home.cases.aCase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.Inquiry;
import com.example.findme.classes.cases.InquiryAdapter;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class CaseInquiriesActivity extends AppCompatActivity implements Database.CaseUpdateListener, InquiryAdapter.OnInquiryClickListener {
    Case aCase;
    String caseId;
    FloatingActionButton btnAddInquiry;
    Dialog dialog;
    LoadingButton loadingButton;
    EditText etName, etDetails;
    List<Inquiry> inquiryItems = new ArrayList<>();
    InquiryAdapter adapter;
    RecyclerView recyclerView;
    boolean fullNameEnabled = false, inquiryDetailsEnabled = false;
    final String fullNameRegex = "\\p{L}{2,15} \\p{L}{2,15}", detailsRegex = "[\\s_]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_inquiries);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");

        recyclerView = findViewById(R.id.recyclerViewInquiries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InquiryAdapter(inquiryItems, this);
        recyclerView.setAdapter(adapter);

        updateInquiryList();
        enableAddInquiryButton();

        Database.listenForCaseUpdates(this, caseId);
    }

    private void enableAddInquiryButton() {
        btnAddInquiry = findViewById(R.id.btnAddInquiry);
        btnAddInquiry.setOnClickListener(view1 -> {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_inquiry);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            etName = dialog.findViewById(R.id.etName);
            etDetails = dialog.findViewById(R.id.etDetails);
            loadingButton = dialog.findViewById(R.id.btnUpload);

            dialog.show();

            etName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String name = editable.toString();

                    if(name.matches(fullNameRegex)) {
                        fullNameEnabled = true;

                        if(fullNameEnabled && inquiryDetailsEnabled) {
                            loadingButton.setEnabled();
                        } else {
                            loadingButton.setDisabled();
                        }
                    } else {
                        fullNameEnabled = false;
                        loadingButton.setDisabled();
                    }
                }
            });

            etDetails.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String details = editable.toString();

                    if(loadingButton.isLocked()) {
                        return;
                    }

                    if(!details.matches(detailsRegex)) {
                        inquiryDetailsEnabled = true;

                        if(fullNameEnabled && inquiryDetailsEnabled) {
                            loadingButton.setEnabled();
                        } else {
                            loadingButton.setDisabled();
                        }
                    } else {
                        inquiryDetailsEnabled = false;
                        loadingButton.setDisabled();
                    }
                }
            });


            dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
            loadingButton.setOnClickListener(v -> {
                if(loadingButton.isInProgress()) {
                    return;
                }

                etName.setEnabled(false);
                etDetails.setEnabled(false);

                loadingButton.start();
                uploadInquiry();
            });
        });
    }

    private void uploadInquiry() {
        Inquiry inquiry = new Inquiry(Database.userId, etName.getText().toString(), etDetails.getText().toString(), System.currentTimeMillis());

        if(!Database.isNetworkConnected(CaseInquiriesActivity.this)) {
            new ErrorDialog(
                    CaseInquiriesActivity.this,
                    "בעיה ברשת",
                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                    "חזור"
            ).show();

            return;
        }

        FirebaseFirestore.getInstance().collection("cases").document(caseId)
                .update("inquiries", FieldValue.arrayUnion(inquiry))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        loadingButton.stop();
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new ErrorDialog(
                                CaseInquiriesActivity.this,
                                "הוספת תשאול",
                                "מסיבה לא ידועה כניסה לפעילות בתיק לא צלחה. נסה שוב בשביל להיכנס לפעילות",
                                "סגור"
                        ).show();

                        etName.setEnabled(true);
                        etDetails.setEnabled(true);
                        loadingButton.stop();
                    }
                });
    }

    private void updateInquiryList() {
        recyclerView.setVisibility(View.GONE);
        findViewById(R.id.tvNoInquiryExist).setVisibility(View.GONE);
        findViewById(R.id.pbLoadingInquiries).setVisibility(View.VISIBLE);

        inquiryItems.clear();
        inquiryItems.addAll(aCase.getInquiries());

        adapter.notifyDataSetChanged();

        if(inquiryItems.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.pbLoadingInquiries).setVisibility(View.GONE);

        } else {
            findViewById(R.id.tvNoInquiryExist).setVisibility(View.VISIBLE);
            findViewById(R.id.pbLoadingInquiries).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;

        updateInquiryList();

        ((TextView) findViewById(R.id.tvCaseId)).setText("מספר תיק: " + aCase.getCaseId());
        btnAddInquiry.setVisibility(aCase.isActive() && aCase.isUserInCase(Database.userId) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onInquiryClick(int position) {
        Dialog customDialog = new Dialog(CaseInquiriesActivity.this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_inquiry);
        customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        customDialog.findViewById(R.id.btnClose).setOnClickListener(v -> customDialog.dismiss());

        Inquiry inquiry = aCase.getInquiries().get(position);

        EditText etName = customDialog.findViewById(R.id.etName),
                etDetails = customDialog.findViewById(R.id.etDetails);

        etName.setText(inquiry.getInvestigatedName());
        etDetails.setText(inquiry.getInquirySummary());

        FirebaseFirestore.getInstance().collection("users")
                .document(inquiry.getUserId())
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        String userName = "המשתמש אינו קיים";
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            userName = user.getFirstName() + " " + user.getLastName();
                        }

                        ((TextView) customDialog.findViewById(R.id.tvFooter)).setText("● תושאל על ידי: " + userName);
                    }
                });


        deleteInquiry(customDialog, inquiry, position);
        customDialog.show();
    }

    private void deleteInquiry(Dialog customDialog, Inquiry inquiry, int position) {
        ImageButton btnDelete = customDialog.findViewById(R.id.btnDelete);

        if(Database.user != null && Database.user.getRole() > 1 && aCase.isUserInCase(Database.userId) && aCase.isActive()) {
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ConfirmDialog(
                            CaseInquiriesActivity.this,
                            "מחק ,תשאול",
                            "האם את/ה בטוח/ה שאת/ה רוצה למחוק את התשאול?",
                            "מחק",
                            "ביטול",
                            new ConfirmClickListener() {
                                @Override
                                public void onOkClicked() {
                                    Inquiry databaseInquiry = aCase.getInquiries().get(position);
                                    if(inquiry.getInquirySummary().equals(databaseInquiry.getInquirySummary()) &&
                                        inquiry.getUploadTime() == databaseInquiry.getUploadTime()) {

                                        aCase.getInquiries().remove(position);
                                        aCase.setInquiries(aCase.getInquiries());

                                        if(!Database.isNetworkConnected(CaseInquiriesActivity.this)) {
                                            new ErrorDialog(
                                                    CaseInquiriesActivity.this,
                                                    "בעיה ברשת",
                                                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                                    "חזור"
                                            ).show();

                                            return;
                                        }

                                        FirebaseFirestore.getInstance().collection("cases")
                                                .document(caseId)
                                                .update("inquiries", aCase.getInquiries())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        customDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        new ErrorDialog(
                                                                CaseInquiriesActivity.this,
                                                                "מחיקה לא צלחה",
                                                                "מסיבה לא ידועה מחיקת התשאול לא צלחה. נסה שוב בשביל למחוק.",
                                                                "סגור"
                                                        ).show();
                                                    }
                                                });
                                    } else {
                                        new ErrorDialog(
                                                CaseInquiriesActivity.this,
                                                "מחיקה לא צלחה",
                                                "משתמש יקר, ככל הנראה התשאול שאת/ה מנסה לנחוק נמחק כבר.",
                                                "סגור"
                                        ).show();

                                        customDialog.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelClick() {

                                }
                            }
                    ).show();
                }
            });
        }
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