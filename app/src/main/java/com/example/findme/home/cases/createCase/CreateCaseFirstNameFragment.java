package com.example.findme.home.cases.createCase;

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


public class CreateCaseFirstNameFragment extends Fragment {
    View view;
    ErrorEditText etFirstName;
    LoadingButton btnSave;
    CreatingCaseListener creatingCaseListener;

    public CreateCaseFirstNameFragment(CreatingCaseListener listener) {
        creatingCaseListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_case_first_name, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingCaseListener.onBackPressed(btnSave.isLocked()));

        etFirstName = view.findViewById(R.id.etFirstName);
        btnSave = view.findViewById(R.id.btnSave);

        etFirstName.addTextChangedListener(EditTextListener.firstName(etFirstName, btnSave));

        String caseId = getArguments().getString("caseId");

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

                Database.isCaseExist(caseId, new Database.IsCaseExistListener() {
                    @Override
                    public void onCaseExist() {
                        etFirstName.setError("מספר תיק כבר קיים במערכת.");
                        btnSave.setLocked(true);
                        btnSave.setDisabled();
                        btnSave.stop();
                    }

                    @Override
                    public void onCaseNotExist() {
                        creatingCaseListener.onCaseCreationResult(new Intent().putExtra("firstName", firstName));
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

        if(getArguments().getBoolean("isCaseTaken")) {
            etFirstName.setError("מספר תיק כבר קיים במערכת.");
            btnSave.setLocked(true);
            btnSave.setDisabled();
        }
    }
}