package com.example.findme.home.cases.aCase;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;

import com.example.findme.classes.cases.Case;
import com.example.findme.classes.cases.MediaAdapter;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.LoadingButton;
import com.example.findme.classes.users.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class CaseMediaActivity extends AppCompatActivity implements MediaAdapter.OnMediaClickListener, Database.CaseUpdateListener {
    Case aCase;
    FloatingActionButton btnAddMedia;
    String caseId;
    Dialog dialog;
    LoadingButton loadingButton;
    AppCompatButton btnUpload;
    EditText etDescription;
    List<StorageReference> mediaItems = new ArrayList<>();
    MediaAdapter mediaAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_media);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");

        recyclerView = findViewById(R.id.recyclerViewMedia);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mediaAdapter = new MediaAdapter(mediaItems, this);
        recyclerView.setAdapter(mediaAdapter);

        enableAddMediaButton();
        updateMediaList();

        Database.listenForCaseUpdates(this, caseId);
    }

    private void updateMediaList() {
        recyclerView.setVisibility(View.GONE);
        findViewById(R.id.tvNoMediaExist).setVisibility(View.GONE);
        findViewById(R.id.pbLoadingMedia).setVisibility(View.VISIBLE);

        mediaItems.clear();

        FirebaseStorage.getInstance().getReference().child("cases/" + caseId + "/media")
                .listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        mediaItems.addAll(listResult.getItems());
                        mediaAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(mediaAdapter.getItemCount() - 1);

                        recyclerView.setVisibility(View.VISIBLE);
                        findViewById(R.id.pbLoadingMedia).setVisibility(View.GONE);

                        if(mediaItems.size() == 0) {
                            findViewById(R.id.tvNoMediaExist).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void enableAddMediaButton() {
        btnAddMedia = findViewById(R.id.btnAddMedia);
        btnAddMedia.setOnClickListener(view1 -> {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_media);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            etDescription = dialog.findViewById(R.id.editText);
            btnUpload = dialog.findViewById(R.id.btnSelectMedia);
            loadingButton = dialog.findViewById(R.id.btnUpload);

            dialog.show();

            btnUpload.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*, video/*");
                mGetContent.launch(intent);
            });

            dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        });
    }

    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        String mediaType = getMediaType(getContentResolver().getType(uri));

                        Drawable drawable = getResources().getDrawable(R.drawable.icon_file_download_done);
                        drawable.setTint(Color.WHITE);
                        btnUpload.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                        btnUpload.setText(mediaType.equals("image/jpeg") ? "נבחרה תמונה" : "נבחר סרטון");
                        btnUpload.setEnabled(false);
                        btnUpload.setAlpha(0.5f);

                        loadingButton.setEnabled();
                        loadingButton.setOnClickListener(v -> {
                            if(loadingButton.isInProgress()) {
                                return;
                            }

                            etDescription.setEnabled(false);
                            loadingButton.start();
                            uploadMedia(uri, mediaType);
                        });
                    } else if(result.getResultCode() != RESULT_CANCELED) {
                        new ErrorDialog(
                                CaseMediaActivity.this,
                                "סיבה לא ידועה",
                                "אירעה שגיאה לא ידועה במהלך העלאת התמונה או הסרטון. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                "סגירה"
                        ).show();
                    }
                }
            });

    private String getMediaType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image")) {
            return "image/jpeg";
        } else if (mimeType != null && mimeType.startsWith("video")) {
            return  "video/mp4";
        } else {
            //Unsupported file type
            return null;
        }
    }

    private void uploadMedia(Uri uri, String mediaType) {
        String type = mediaType.equals("image/jpeg") ? ".jpeg" : ".mp4";

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("cases/" + caseId + "/media/" + System.currentTimeMillis() + type);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(mediaType)
                .setCustomMetadata("userId", Database.userId)
                .setCustomMetadata("description", etDescription.getText().toString().trim().matches("[\\s_]+") ? "" : etDescription.getText().toString().trim())
                .build();

        if(aCase.isActive() && aCase.isUserInCase(Database.userId)) {
            fileRef.putFile(uri, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    loadingButton.stop();
                    dialog.dismiss();

                    updateMediaList();
                }
            });
        } else {
            new ErrorDialog(
                    this,
                    "אין גישה",
                    "משתמש יקר, ככל הנראה התיק נסגר או שהוסרת מפעילות בתיק.",
                    "סגירה"
            ).show();

            loadingButton.stop();
            dialog.dismiss();
        }
    }

    @Override
    public void onMediaClick(Uri uri, boolean isVideo) {
        Dialog customDialog = new Dialog(com.example.findme.home.cases.aCase.CaseMediaActivity.this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_media);
        customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        customDialog.findViewById(R.id.btnClose).setOnClickListener(v -> customDialog.dismiss());

        ImageView imageView = customDialog.findViewById(R.id.ivImage);
        Glide.with(com.example.findme.home.cases.aCase.CaseMediaActivity.this).load(uri).into(imageView);

        if(isVideo) {
            customDialog.findViewById(R.id.ivPlay).setVisibility(View.VISIBLE);
            ((TextView) customDialog.findViewById(R.id.tvTitle)).setText("לחץ על הסרטון כדי להפעיל");
        }

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            if(isVideo) {
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "image/*");
            }

            startActivity(intent);
        });

        FirebaseStorage.getInstance().getReferenceFromUrl(uri.toString())
                .getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        EditText etDescription = customDialog.findViewById(R.id.etDescription);
                        String description = storageMetadata.getCustomMetadata("description");

                        if(description != null && !description.isEmpty()) {
                            etDescription.setText(description);
                            etDescription.setVisibility(View.VISIBLE);
                        }

                        FirebaseFirestore.getInstance().collection("users")
                                .document(storageMetadata.getCustomMetadata("userId"))
                                .get(Source.SERVER)
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot document) {
                                        String userName = "המשתמש אינו קיים";
                                        if (document != null && document.exists()) {
                                            User user = document.toObject(User.class);
                                            userName = user.getFirstName() + " " + user.getLastName();
                                        }

                                        ((TextView) customDialog.findViewById(R.id.tvFooter)).setText("● הועלה על ידי: " + userName);
                                    }
                                });
                    }
                });


        deleteMedia(customDialog, uri, isVideo);

        customDialog.show();
    }

    private void deleteMedia(Dialog customDialog, Uri uri, boolean isVideo) {
        ImageButton btnDelete = customDialog.findViewById(R.id.btnDelete);
        if(!aCase.isActive()) {
            btnDelete.setVisibility(View.GONE);
        }

        if(Database.user != null && Database.user.getRole() > 1 && aCase.isUserInCase(Database.userId) && aCase.isActive()) {
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String mediaType = isVideo ? "סרטון" : "תמונה";

                    new ConfirmDialog(
                            CaseMediaActivity.this,
                            "מחק " + mediaType,
                            "האם את/ה בטוח/ה שאת/ה רוצה למחוק את " + mediaType + "?",
                            "מחק",
                            "ביטול",
                            new ConfirmClickListener() {
                                @Override
                                public void onOkClicked() {
                                    FirebaseStorage.getInstance().getReferenceFromUrl(uri.toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            if(customDialog != null) {
                                                customDialog.dismiss();
                                            }

                                            updateMediaList();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            new ErrorDialog(
                                                    CaseMediaActivity.this,
                                                    "מחיקה לא צלחה",
                                                    "מסיבה לא ידועה מחיקת ה"+ mediaType +"לא צלחה. נסה שוב בשביל למחוק.",
                                                    "סגור"
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
            });
        }
    }

    @Override
    public void onCaseUpdated(Case updatedCase, String updatedCaseId) {
        aCase = updatedCase;
        caseId = updatedCaseId;

        ((TextView) findViewById(R.id.tvCaseId)).setText("מספר תיק: " + aCase.getCaseId());
        btnAddMedia.setVisibility(aCase.isActive() && aCase.isUserInCase(Database.userId) ? View.VISIBLE : View.GONE);
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