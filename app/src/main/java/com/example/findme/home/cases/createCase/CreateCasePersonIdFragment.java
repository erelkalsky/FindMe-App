package com.example.findme.home.cases.createCase;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.findme.R;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.ui.EditTextListener;
import com.example.findme.classes.ui.ErrorEditText;
import com.example.findme.classes.api.Database;

public class CreateCasePersonIdFragment extends Fragment {
    View view;
    ErrorEditText etPersonId;
    LoadingButton btnSave;
    CreatingCaseListener creatingCaseListener;

    public CreateCasePersonIdFragment(CreatingCaseListener listener) {
        creatingCaseListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_case_person_id, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingCaseListener.onBackPressed(btnSave.isLocked()));

        etPersonId = view.findViewById(R.id.etPersonId);
        btnSave = view.findViewById(R.id.btnSave);

        etPersonId.addTextChangedListener(EditTextListener.personId(etPersonId, btnSave));

        String caseId = getArguments().getString("caseId");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String personId = etPersonId.getText().toString();
                etPersonId.clearFocus();

                if(btnSave.isInProgress()) {
                    return;
                }

                etPersonId.removeError();
                btnSave.start();

                Database.isCaseExist(caseId, new Database.IsCaseExistListener() {
                    @Override
                    public void onCaseExist() {
                        etPersonId.setError("מספר תיק כבר קיים במערכת.");
                        btnSave.setLocked(true);
                        btnSave.setDisabled();
                        btnSave.stop();
                    }

                    @Override
                    public void onCaseNotExist() {
                        creatingCaseListener.onCaseCreationResult(new Intent().putExtra("personId", personId));
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
            etPersonId.setError("מספר תיק כבר קיים במערכת.");
            btnSave.setLocked(true);
            btnSave.setDisabled();
        }
    }
}