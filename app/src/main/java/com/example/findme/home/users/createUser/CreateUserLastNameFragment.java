package com.example.findme.home.users.createUser;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.EditTextListener;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.ui.LoadingButton;

public class CreateUserLastNameFragment extends Fragment {
    View view;
    LoadingButton btnSave;
    ErrorEditText etLastName;
    CreatingUserListener creatingUserListener;

    public CreateUserLastNameFragment(CreatingUserListener listener) {
        creatingUserListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_create_user_last_name, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingUserListener.onBackPressed(btnSave.isLocked()));

        etLastName = view.findViewById(R.id.etLastName);
        btnSave = view.findViewById(R.id.btnSave);

        etLastName.addTextChangedListener(EditTextListener.lastName(etLastName, btnSave));

        String phone = getArguments().getString("phone");

        if(Database.user.getRole() == 2) {
            btnSave.setButtonText("צור משתמש");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastName = etLastName.getText().toString();
                etLastName.clearFocus();

                if(btnSave.isInProgress()) {
                    return;
                }

                etLastName.removeError();
                btnSave.start();

                Database.isPhoneExist(phone, new Database.IsPhoneExistListener() {
                    @Override
                    public void onPhoneExist() {
                        etLastName.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
                        btnSave.setLocked(true);
                        btnSave.setDisabled();
                        btnSave.stop();
                    }

                    @Override
                    public void onPhoneNotExist() {
                        creatingUserListener.onUserCreationResult(new Intent().putExtra("lastName", lastName));
                        btnSave.stop();
                    }


                    @Override
                    public void onFailure(String errorTitle, String errorMessage, String buttonText) {
                        new ErrorDialog(
                                requireContext(),
                                errorTitle,
                                errorMessage,
                                buttonText
                        ).show();
                        btnSave.stop();
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getArguments().getBoolean("isPhoneTaken")) {
            etLastName.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
            btnSave.setLocked(true);
            btnSave.setDisabled();
        }
    }

    public void failedToCreateUser() {
        etLastName.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
        btnSave.setLocked(true);
        btnSave.setDisabled();
        btnSave.stop();
    }
}