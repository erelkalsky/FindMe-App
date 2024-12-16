package com.example.findme.home.cases.aCase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.UsersInCaseAdapter;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.SearchEditText;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CaseUsersInCaseActivity extends AppCompatActivity implements Database.CaseUpdateListener, UsersInCaseAdapter.OnUsersInCaseClickListener {
    Case aCase;
    String caseId;
    List<String> userListId = new ArrayList<>();
    UsersInCaseAdapter adapter;
    SearchEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_users_in_case);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");
        userListId = aCase.getUsersInCase();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UsersInCaseAdapter(userListId, this);
        recyclerView.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.setFilter(editable.toString());
                adapter.notifyDataSetChanged();
            }

        });


        Database.listenForCaseUpdates(this, caseId);
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;


        if(!checkForNull()) {
            userListId.clear();
            userListId.addAll(aCase.getUsersInCase());

            adapter.notifyDataSetChanged();
        }

        ((TextView) findViewById(R.id.tvCaseId)).setText("מספר תיק: " + aCase.getCaseId());
    }

    public boolean checkForNull() {
        for(String userId: aCase.getUsersInCase()) {
            if(userId == null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onUserClick(User user, String userId , View view) {
        if(!aCase.isActive()) {
            view.setForeground(null);

            return;
        }

        if(Database.userId == userId) {
            new ErrorDialog(
                    this,
                    "אין גישה",
                    "משתמש יקר, בשביל להוציא את עצמך מפעילות בתיק גש/י לעמוד התיק והורד/י את פעילותך.",
                    "סגור"
            ).show();
        } else if(user.getRole() > Database.user.getRole() - 1) {
            new ErrorDialog(
                    this,
                    "אין גישה",
                    "משתמש יקר, אין לך גישה בשביל להוציא את משתמש זה מפעילות בתיק.",
                    "סגור"
            ).show();
        } else {
            new ConfirmDialog(
                    this,
                    "הוצאה מפעילות בתיק",
                    "האם את/ה בטוח/ה שאת/ה רוצה להוציא את " + user.getFirstName() + " " + user.getLastName() + " מפעילות בתיק?",
                    "הוצאה",
                    "ביטול",
                    new ConfirmClickListener() {
                        @Override
                        public void onOkClicked() {
                            if(!Database.isNetworkConnected(CaseUsersInCaseActivity.this)) {
                                new ErrorDialog(
                                        CaseUsersInCaseActivity.this,
                                        "בעיה ברשת",
                                        "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                        "חזור"
                                ).show();

                                return;
                            }

                            FirebaseFirestore.getInstance().collection("cases").document(caseId)
                                    .update("usersInCase", FieldValue.arrayRemove(userId))
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            new ErrorDialog(
                                                    CaseUsersInCaseActivity.this,
                                                    "סיבה לא ידועה",
                                                    "אירעה שגיאה לא ידועה במהלך הוצאת המשתמש מפעילות בתיק. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                    "חזור"
                                            ).show();
                                        }
                                    });
                        }

                        @Override
                        public void onCancelClick() {

                        }
                    }
            ).show();
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