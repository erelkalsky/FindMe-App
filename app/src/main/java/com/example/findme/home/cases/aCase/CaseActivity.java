package com.example.findme.home.cases.aCase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CaseActivity extends AppCompatActivity implements Database.CaseUpdateListener, Database.UserUpdateListener {
    Case aCase;
    String caseId;
    TextView tvCaseId, tvCaseTranscript, tvPersonDetails;
    ImageView ivCaseImage;
    Switch switchUserInCase;
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");

        enableButtons();
        findViews();

        Database.listenForCaseUpdates(this, caseId);
        Database.listenForUserUpdates(this);
    }

    private void findViews() {
        tvCaseId = findViewById(R.id.tvCaseId);
        tvCaseTranscript = findViewById(R.id.tvCaseTranscript);
        tvPersonDetails = findViewById(R.id.tvPersonDetails);
        ivCaseImage = findViewById(R.id.ivCaseImage);
    }

    private void enableButtons() {
        findViewById(R.id.btnEditCasePic).setOnClickListener(v -> openImageDialog());

        findViewById(R.id.btnTranscript).setOnClickListener(view -> {
            startEditCase("transcript", "תמלול תיק",  aCase.getTranscript(), "big");
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnCaseId).setOnClickListener(view -> {
            startEditCase("caseId", "מספר תיק",  aCase.getCaseId(), "number");
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnPersonDetails).setOnClickListener(view -> {
            startActivity(new Intent(CaseActivity.this, MissingPersonActivity.class)
                    .putExtra("case", aCase)
                    .putExtra("caseId", caseId)
            );

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });


        findViewById(R.id.btnMedia).setOnClickListener(view -> {
            startActivity(
                    new Intent(CaseActivity.this, CaseMediaActivity.class)
                            .putExtra("caseId", caseId)
                            .putExtra("case", aCase)
                            .putExtra("caseNumber", aCase.getCaseId())
            );

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnMaps).setOnClickListener(v -> {
            startActivity(
                    new Intent(CaseActivity.this, CaseMapsActivity.class)
                            .putExtra("caseId", caseId)
                            .putExtra("case", aCase)
                            .putExtra("caseNumber", aCase.getCaseId())
            );

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnInquiries).setOnClickListener(v -> {
            startActivity(
                    new Intent(CaseActivity.this, CaseInquiriesActivity.class)
                            .putExtra("caseId", caseId)
                            .putExtra("case", aCase)
                            .putExtra("caseNumber", aCase.getCaseId())
            );

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnUsersInCase).setOnClickListener(v -> {
            if(!Database.isNetworkConnected(CaseActivity.this)) {
                new ErrorDialog(
                        CaseActivity.this,
                        "בעיה ברשת",
                        "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                        "חזור"
                ).show();

                return;
            }

            startActivity(
                    new Intent(CaseActivity.this, CaseUsersInCaseActivity.class)
                            .putExtra("caseId", caseId)
                            .putExtra("case", aCase)
                            .putExtra("caseNumber", aCase.getCaseId())
            );

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        findViewById(R.id.btnCloseCase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ConfirmDialog(
                        CaseActivity.this,
                        aCase.isActive() ? "סגור תיק" :  "החזר תיק לפעילות",
                        "האם את/ה בטוח/ה שאת/ה רוצה " + (aCase.isActive() ? "לסגור את התיק?" : "להחזיר את התיק לפעילות?"),
                        aCase.isActive() ? "סגור תיק" :  "החזר לפעילות",
                        "ביטול",
                        new ConfirmClickListener() {
                            @Override
                            public void onOkClicked() {
                                if(!Database.isNetworkConnected(CaseActivity.this)) {
                                    new ErrorDialog(
                                            CaseActivity.this,
                                            "בעיה ברשת",
                                            "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                            "חזור"
                                    ).show();

                                    return;
                                }

                                FirebaseFirestore.getInstance().collection("cases")
                                        .document(caseId)
                                        .update("active", !aCase.isActive()).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                new ErrorDialog(
                                                        CaseActivity.this,
                                                        "עידכון לא צלח",
                                                        "מסיבה לא ידועה " + (aCase.isActive() ? "סגירת התיק" :  "החזרת התיק לפעילות") + " לא צלחה, נסה שוב.",
                                                        "סגור"
                                                ).show();;
                                            }
                                        });
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        }
                ).show();
            }
        });

        switchUserInCase = findViewById(R.id.switchUserInCase);
        switchUserInCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    if(!aCase.isUserInCase(Database.userId)) {
                        if(!Database.isNetworkConnected(CaseActivity.this)) {
                            new ErrorDialog(
                                    CaseActivity.this,
                                    "בעיה ברשת",
                                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                    "חזור"
                            ).show();

                            switchUserInCase.setChecked(aCase.isUserInCase(Database.userId) ? true : false);

                            return;
                        }

                        if(Database.userId == null) {
                            new ErrorDialog(
                                    CaseActivity.this,
                                    "פעילות בתיק",
                                    "מסיבה לא ידועה כניסה לפעילות בתיק לא צלחה. נסה שוב בשביל להיכנס לפעילות",
                                    "סגור"
                            ).show();

                            return;
                        }

                        FirebaseFirestore.getInstance().collection("cases").document(caseId)
                                .update("usersInCase", FieldValue.arrayUnion(Database.userId))
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new ErrorDialog(
                                                CaseActivity.this,
                                                "פעילות בתיק",
                                                "מסיבה לא ידועה כניסה לפעילות בתיק לא צלחה. נסה שוב בשביל להיכנס לפעילות",
                                                "סגור"
                                        ).show();

                                        switchUserInCase.setChecked(aCase.isUserInCase(Database.userId) ? true : false);
                                    }
                                });
                    }
                } else {
                    if(aCase.isUserInCase(Database.userId)) {
                        if(!Database.isNetworkConnected(CaseActivity.this)) {
                            new ErrorDialog(
                                    CaseActivity.this,
                                    "בעיה ברשת",
                                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                    "חזור"
                            ).show();

                            switchUserInCase.setChecked(aCase.isUserInCase(Database.userId) ? true : false);
                            return;
                        }

                        if(Database.userId == null) {
                            new ErrorDialog(
                                    CaseActivity.this,
                                    "פעילות בתיק",
                                    "מסיבה לא ידועה יציאה מפעילות בתיק לא צלחה. נסה שוב בשביל לצאת מפעילות",
                                    "סגור"
                            ).show();

                            return;
                        }

                        FirebaseFirestore.getInstance().collection("cases").document(caseId)
                                .update("usersInCase", FieldValue.arrayRemove(Database.userId))
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        new ErrorDialog(
                                                CaseActivity.this,
                                                "פעילות בתיק",
                                                "מסיבה לא ידועה יציאה מפעילות בתיק לא צלחה. נסה שוב בשביל לצאת מפעילות",
                                                "סגור"
                                        ).show();

                                        switchUserInCase.setChecked(aCase.isUserInCase(Database.userId) ? true : false);
                                    }
                                });
                    }
                }
            }
        });
    }

    private void updateUi() {
        tvCaseTranscript.setText(aCase.getTranscript());
        tvCaseId.setText(aCase.getCaseId());
        tvPersonDetails.setText(aCase.getMissingPerson().getFirstName() + " " + aCase.getMissingPerson().getLastName() + " (ת\"ז: " + aCase.getMissingPerson().getId() + ")");

        ((TextView) findViewById(R.id.tvUserInCase)).setText(aCase.isUserInCase(Database.userId) ? "פעיל" : "לא פעיל");
        ((TextView) findViewById(R.id.tvAmountUserInCase)).setText("מספר משתמשים פעילים: " + aCase.getUsersInCase().size());
        switchUserInCase.setChecked(aCase.isUserInCase(Database.userId) ? true : false);

        findViewById(R.id.switchUserInCaseDvider).setVisibility(aCase.isActive() ? View.VISIBLE : View.GONE);
        findViewById(R.id.switchUserInCaseHolder).setVisibility(aCase.isActive() ? View.VISIBLE : View.GONE);

        findViewById(R.id.btnEditCasePic).setVisibility(aCase.isActive() && aCase.isUserInCase(Database.userId) ? View.VISIBLE : View.GONE);
        findViewById(R.id.viewDivider).setVisibility(aCase.isActive() && aCase.isUserInCase(Database.userId) ? View.GONE : View.VISIBLE);

        ((TextView) findViewById(R.id.tvCaseStatus)).setText(aCase.isActive() ? "פעיל" : "סגור");
        ((TextView) findViewById(R.id.tvCaseStatusClose)).setText(aCase.isActive() ? "סגור תיק" :  "החזר תיק לפעילות");
    }

    private void startEditCase(String fieldName, String fieldNameHebrew, String value, String keyboard) {
        startActivity(
                new Intent(CaseActivity.this, EditCaseActivity.class)
                        .putExtra("fieldName", fieldName)
                        .putExtra("fieldNameHebrew", fieldNameHebrew)
                        .putExtra("value", (String) value)
                        .putExtra("keyboard", keyboard)
                        .putExtra("caseId", caseId)
                        .putExtra("toUpdate", fieldName)
        );

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void openImageDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(CaseActivity.this);
        dialog.setContentView(R.layout.dialog_edit_user_image);
        dialog.findViewById(R.id.btnChooseFromCamera).setVisibility(View.GONE);
        dialog.show();

        dialog.findViewById(R.id.btnChooseFromGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmDialog confirmDialog = new ConfirmDialog(
                        CaseActivity.this,
                        "מחיקת תמונה פרופיל",
                        "האם את/ה בטוח/ה שאת/ה רוצה למחוק את תמונה הפרופיל",
                        "מחק",
                        "ביטול",
                        new ConfirmClickListener() {
                            @Override
                            public void onOkClicked() {
                                FirebaseStorage.getInstance().getReference("cases").child(caseId).child("caseImage")
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                ivCaseImage.setImageDrawable(getDrawable(R.drawable.img_default_profile));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                ErrorDialog errorDialog = new ErrorDialog(
                                                        CaseActivity.this,
                                                        "סיבה לא ידועה",
                                                        "אירעה שגיאה לא ידועה במהלך מחיקת התמונה. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                        "חזור"
                                                );
                                                errorDialog.show();
                                            }
                                        });

                                dialog.dismiss();
                            }

                            @Override
                            public void onCancelClick() {

                            }
                        });

                confirmDialog.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropMenuCropButtonTitle("סיום")
                    .start(this);
        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();

                FirebaseStorage.getInstance().getReference("cases").child(caseId).child("caseImage").putFile(croppedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ivCaseImage.setImageURI(croppedImageUri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                new ErrorDialog(
                                        CaseActivity.this,
                                        "סיבה לא ידועה",
                                        "אירעה שגיאה לא ידועה במהלך החלפת התמונה. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                        "חזור"
                                ).show();
                            }
                        });
            }
        }
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;

        if(isVisible) {
            FirebaseStorage.getInstance().getReference("cases").child(caseId).child("caseImage")
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(CaseActivity.this).load(uri).into(ivCaseImage);
                        }
                    });
        }

        updateUsersInCase();
        updateUi();
    }

    private void updateUsersInCase() {
        for(int i = 0; i < aCase.getUsersInCase().size(); i++) {
            String userId = aCase.getUsersInCase().get(i);
            if(userId != null) {
                FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .get(Source.SERVER)
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot document) {
                                if (!(document != null && document.exists())) {
                                    FirebaseFirestore.getInstance().collection("cases").document(caseId)
                                            .update("usersInCase", FieldValue.arrayRemove(userId));
                                }
                            }
                        });
            } else {
                aCase.removeUserFromCase(null);
                FirebaseFirestore.getInstance().collection("cases").document(caseId)
                        .update("usersInCase", aCase.getUsersInCase());
            }
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

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onUserUpdated() {
        if(Database.user != null) {
            if(Database.user.getRole() == 1) {
                setCloseCaseVisibility(false);
            } else {
                setCloseCaseVisibility(true);
            }
        } else {
            setCloseCaseVisibility(false);
        }
    }

    public void setCloseCaseVisibility(boolean isVisible) {
        findViewById(R.id.btnCloseCase).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        findViewById(R.id.viewDividerClose).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserDeleted() {
        finish();
    }
}