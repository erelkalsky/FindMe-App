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

public class CreateUserFirstNameFragment extends Fragment {
    View view;
    LoadingButton btnSave;
    ErrorEditText etFirstName;
    CreatingUserListener creatingUserListener;

    public CreateUserFirstNameFragment(CreatingUserListener listener) {
        creatingUserListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_user_first_name, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingUserListener.onBackPressed(btnSave.isLocked()));

        etFirstName = view.findViewById(R.id.etFirstName);
        btnSave = view.findViewById(R.id.btnSave);

        etFirstName.addTextChangedListener(EditTextListener.firstName(etFirstName, btnSave));

        String phone = getArguments().getString("phone");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = etFirstName.getText().toString();
                etFirstName.clearFocus();

                if(btnSave.isInProgress()) {
                    return;
                }

                etFirstName.removeError();
                btnSave.start();

                Database.isPhoneExist(phone, new Database.IsPhoneExistListener() {
                    @Override
                    public void onPhoneExist() {
                        etFirstName.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות אימייל.");
                        btnSave.setLocked(true);
                        btnSave.setDisabled();
                        btnSave.stop();
                    }

                    @Override
                    public void onPhoneNotExist() {
                        creatingUserListener.onUserCreationResult(new Intent().putExtra("firstName", firstName));
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
            etFirstName.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
            btnSave.setLocked(true);
            btnSave.setDisabled();
        }
    }
}