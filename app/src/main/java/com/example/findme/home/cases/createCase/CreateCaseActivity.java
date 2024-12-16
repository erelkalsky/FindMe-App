package com.example.findme.home.cases.createCase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.MissingPerson;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.dialogs.LoadingDialog;

public class CreateCaseActivity extends AppCompatActivity implements CreatingCaseListener {
    private final Fragment[] fragments = new Fragment[] {
            new CreateCaseIdFragment(this),
            new CreateCasePersonIdFragment(this),
            new CreateCaseFirstNameFragment(this),
            new CreateCaseLastNameFragment(this),
            new CreateCaseTranscriptFragment(this)
    };

    Fragment currentFragment;
    String caseId, personId, firstName, lastName, transcript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_case);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragments[0]);
        fragmentTransaction.commit();

        currentFragment = fragments[0];
    }

    @Override
    public void onBackPressed() {
        onBackPressed(false);
    }

    @Override
    public void onBackPressed(boolean isCaseTaken) {
        if (currentFragment.equals(fragments[1])) {
            loadFragment(fragments[0], false, isCaseTaken);
        } else if (currentFragment.equals(fragments[2])) {
            loadFragment(fragments[1], false, isCaseTaken);
        } else if (currentFragment.equals(fragments[3])) {
            loadFragment(fragments[2], false, isCaseTaken);
        } else if (currentFragment.equals(fragments[4])) {
            loadFragment(fragments[3], false, isCaseTaken);
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onCaseCreationResult(Intent data) {
        if (currentFragment.equals(fragments[0])) {
            caseId = data.getStringExtra("caseId");
            loadFragment(fragments[1], true, false);
        } else if (currentFragment.equals(fragments[1])) {
            personId = data.getStringExtra("personId");
            loadFragment(fragments[2], true, false);
        } else if (currentFragment.equals(fragments[2])) {
            firstName = data.getStringExtra("firstName");
            loadFragment(fragments[3], true, false);
        } else if (currentFragment.equals(fragments[3])) {
            lastName = data.getStringExtra("lastName");
            loadFragment(fragments[4], true, false);
        } else if (currentFragment.equals(fragments[4])) {
            transcript = data.getStringExtra("transcript");
            createCase();
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void loadFragment(Fragment fragment, boolean isNext, boolean isPhoneTaken) {
        Bundle bundle = new Bundle();
        if (caseId != null) {
            bundle.putString("caseId", caseId);
        }

        bundle.putBoolean("isCaseTaken", isPhoneTaken);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(isNext) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();

        currentFragment = fragment;
    }

    private void createCase() {
        Case newCase = new Case(caseId, transcript, new MissingPerson(firstName, lastName, personId));

        ConfirmDialog confirmDialog = new ConfirmDialog(
                CreateCaseActivity.this,
                "צור תיק",
                "האם אתה בטוח שהינך רוצה ליצור תיק זה?",
                "צור תיק",
                "ביטול",
                new ConfirmClickListener() {
                    @Override
                    public void onOkClicked() {
                        Database.isCaseExist(caseId, new Database.IsCaseExistListener() {
                            @Override
                            public void onCaseExist() {
                                if(currentFragment == null) {
                                    return;
                                }

                                if(currentFragment instanceof CreateCaseLastNameFragment) {
                                    ((CreateCaseTranscriptFragment) currentFragment).failedToCreateUser();
                                }
                            }

                            @Override
                            public void onCaseNotExist() {
                                addCaseToDatabase(newCase);
                            }

                            @Override
                            public void onFailure(String errorTitle, String errorMessage, String buttonText) {
                                new ErrorDialog(
                                        CreateCaseActivity.this,
                                        errorTitle,
                                        errorMessage,
                                        buttonText
                                ).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelClick() {

                    }
                }
        );

        confirmDialog.show();
    }

    private void addCaseToDatabase(Case newCase) {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Database.createCase(newCase, new Database.CreateCaseListener() {
            @Override
            public void onCreateCaseSuccess() {
                loadingDialog.dismiss();
                finish();
            }

            @Override
            public void onCreateCaseFailure(String errorMessage) {
                loadingDialog.dismiss();

                ErrorDialog errorDialog = new ErrorDialog(
                        CreateCaseActivity.this,
                        "בעיה ביצירת תיק",
                        "חלה בעיה ביצירת התיק החדש, אנא נסה שוב.",
                        "סגור"
                );
                errorDialog.show();
            }
        });
    }
}