package com.example.findme.home.users.createUser;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.DownSelect;
import com.example.findme.classes.ui.LoadingButton;


public class CreateUserRoleFragment extends Fragment {

    LoadingButton btnSave;
    DownSelect downSelect;
    View view;
    CreatingUserListener creatingUserListener;

    public CreateUserRoleFragment(CreatingUserListener listener) {
        creatingUserListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_user_role, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingUserListener.onBackPressed(btnSave.isLocked()));

        downSelect = view.findViewById(R.id.downSelect);
        downSelect.setOnItemSelectedListener(getDropDownSelectedListener());

        String phone = getArguments().getString("phone");

        btnSave = view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int role = downSelect.getItem();

                //int roleId = roleMap.getOrDefault(role, -1);

                if (btnSave.isInProgress()) {
                    return;
                }

                if(role == 0) {
                    btnSave.setDisabled();
                    btnSave.stop();
                }

                btnSave.start();

                Database.isPhoneExist(phone, new Database.IsPhoneExistListener() {
                    @Override
                    public void onPhoneExist() {
                        downSelect.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
                        btnSave.setLocked(true);
                        btnSave.setDisabled();
                        btnSave.stop();
                    }

                    @Override
                    public void onPhoneNotExist() {
                        creatingUserListener.onUserCreationResult(new Intent().putExtra("role", role));
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

    private AdapterView.OnItemSelectedListener getDropDownSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!btnSave.isLocked()) {
                    btnSave.setEnabled();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                btnSave.setDisabled();
            }
        };
    }

    public void failedToCreateUser() {
        downSelect.setError("הטלפון שהזנת קודם נתפס, חזור אחורה לשנות טלפון.");
        btnSave.setLocked(true);
        btnSave.setDisabled();
        btnSave.stop();
    }
}