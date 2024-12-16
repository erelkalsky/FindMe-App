package com.example.findme.home.users.createUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.findme.R;
import com.example.findme.classes.api.SendSms;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.PasswordGenerator;
import com.example.findme.classes.dialogs.LoadingDialog;
import com.example.findme.classes.users.User;


public class CreateUserActivity extends AppCompatActivity implements CreatingUserListener {

    final Fragment[] fragments = new Fragment[] {
            new CreateUserPhoneFragment(this),
            new CreateUserFirstNameFragment(this),
            new CreateUserLastNameFragment(this),
            new CreateUserRoleFragment(this)
    };

    Fragment currentFragment;
    String phone, firstName, lastName;
    int role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

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
    public void onBackPressed(boolean isPhoneTaken) {
        if (currentFragment.equals(fragments[1])) {
            loadFragment(fragments[0], false, isPhoneTaken);
        } else if (currentFragment.equals(fragments[2])) {
            loadFragment(fragments[1], false, isPhoneTaken);
        } else if (currentFragment.equals(fragments[3])) {
            loadFragment(fragments[2], false, isPhoneTaken);
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onUserCreationResult(Intent data) {
        if (currentFragment.equals(fragments[0])) {
            phone = data.getStringExtra("phone");
            loadFragment(fragments[1], true, false);
        } else if (currentFragment.equals(fragments[1])) {
            firstName = data.getStringExtra("firstName");
            loadFragment(fragments[2], true, false);
        } else if (currentFragment.equals(fragments[2])) {
            lastName = data.getStringExtra("lastName");

            if(Database.user.getRole() == 2) {
                role = 1;
                createUser();
            } else {
                loadFragment(fragments[3], true, false);
            }
        } else if (currentFragment.equals(fragments[3])) {
            role = data.getIntExtra("role", -1);
            createUser();
        } else {
            super.finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void loadFragment(Fragment fragment, boolean isNext, boolean isPhoneTaken) {
        Bundle bundle = new Bundle();
        if (phone != null) {
            bundle.putString("phone", phone);
        }

        bundle.putBoolean("isPhoneTaken", isPhoneTaken);
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

    private void createUser() {
        String password = PasswordGenerator.generatePassword();

        User newUser = new User(
                phone,
                password,
                firstName,
                lastName,
                role
        );

        ConfirmDialog confirmDialog = new ConfirmDialog(
                CreateUserActivity.this,
                "צור משתמש",
                "האם אתה בטוח שהינך רוצה ליצור משתמש זה?",
                "צור משתמש",
                "ביטול",
                new ConfirmClickListener() {
                    @Override
                    public void onOkClicked() {
                        Database.isPhoneExist(phone, new Database.IsPhoneExistListener() {
                            @Override
                            public void onPhoneExist() {
                                if(currentFragment == null) {
                                    return;
                                }

                                if(currentFragment instanceof CreateUserLastNameFragment) {
                                    ((CreateUserLastNameFragment) currentFragment).failedToCreateUser();
                                } else if(currentFragment instanceof CreateUserRoleFragment) {
                                    ((CreateUserRoleFragment) currentFragment).failedToCreateUser();
                                }
                            }

                            @Override
                            public void onPhoneNotExist() {
                                addUserToDatabase(newUser);
                            }

                            @Override
                            public void onFailure(String errorTitle, String errorMessage, String buttonText) {
                                new ErrorDialog(
                                        CreateUserActivity.this,
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

    private void addUserToDatabase(User newUser) {
        String body = "שלום " + newUser.getFirstName() + " " + newUser.getLastName() + ", נוצר לך חשבון באפליקציית איתור נעדרים.\n" +
                "תוכל להיכנס עם מספר טלפון וסיסמה.\n" +
                "סיסמתך לאפליקציה היא: " + newUser.getPassword() + "\n" +
                "תוכל לשנות אותה בתוך האפליקציה. המשך יום נעים.\n";

        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Database.createUser(newUser, new Database.CreateUserListener() {
            @Override
            public void onCreateUserSuccess() {
                new SendSms(newUser.getPhone(), body).execute();
                loadingDialog.dismiss();
                finish();
            }

            @Override
            public void onCreateUserFailure(String errorMessage) {
                loadingDialog.dismiss();

                ErrorDialog errorDialog = new ErrorDialog(
                        CreateUserActivity.this,
                        "בעיה ביצירת משתמש",
                        "חלה בעיה ביצירת המשתמש החדש, אנא נסה שוב.",
                        "סגור"
                );
                errorDialog.show();
            }
        });
    }
}