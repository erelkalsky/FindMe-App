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

public class CreateCaseIdFragment extends Fragment {
    View view;
    ErrorEditText etCase;
    LoadingButton btnSave;
    CreatingCaseListener creatingCaseListener;

    public CreateCaseIdFragment(CreatingCaseListener listener) {
        creatingCaseListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_case_id, container, false);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> creatingCaseListener.onBackPressed(false));

        etCase = view.findViewById(R.id.etCase);
        btnSave = view.findViewById(R.id.btnSave);

        etCase.addTextChangedListener(EditTextListener.caseId(etCase, btnSave));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caseId = etCase.getText().toString();
                etCase.clearFocus();

                if(btnSave.isInProgress()) {
                    return;
                }

                etCase.removeError();
                btnSave.start();

                Database.isCaseExist(caseId, new Database.IsCaseExistListener() {
                    @Override
                    public void onCaseExist() {
                        etCase.setError("מספר תיק כבר קיים במערכת.");
                        btnSave.stop();
                    }

                    @Override
                    public void onCaseNotExist() {
                        creatingCaseListener.onCaseCreationResult(new Intent().putExtra("caseId", caseId));
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
}