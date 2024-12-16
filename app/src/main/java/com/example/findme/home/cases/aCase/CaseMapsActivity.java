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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.findme.classes.cases.MapAdapter;
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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class CaseMapsActivity extends AppCompatActivity implements MapAdapter.OnMapClickListener, Database.CaseUpdateListener {
    Case aCase;
    FloatingActionButton btnAddMap;
    String caseId;
    Dialog dialog;
    LoadingButton loadingButton;
    AppCompatButton btnUpload;
    EditText etDescription;
    List<StorageReference> mapItems = new ArrayList<>();
    MapAdapter mapAdapter;
    RecyclerView recyclerView;
    EditText etMapsLink;
    Uri mapUri;
    final String mapLinkRegex = "\\b(?:https?://(?:www\\.)?(?:maps\\.google\\.com/maps|maps\\.apple\\.com|maps\\.gov\\.il|maps\\.app\\.goo\\.gl|apq9h\\.app\\.goo\\.gl)/?[^\\s]*)\\b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_maps);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish());

        aCase = (Case) getIntent().getSerializableExtra("case");
        caseId = getIntent().getStringExtra("caseId");

        recyclerView = findViewById(R.id.recyclerViewMaps);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mapAdapter = new MapAdapter(mapItems, this);
        recyclerView.setAdapter(mapAdapter);

        enableAddMapButton();
        updateMapList();

        Database.listenForCaseUpdates(this, caseId);
    }

    private void updateMapList() {
        recyclerView.setVisibility(View.GONE);
        findViewById(R.id.tvNoMapExist).setVisibility(View.GONE);
        findViewById(R.id.pbLoadingMaps).setVisibility(View.VISIBLE);

        mapItems.clear();

        FirebaseStorage.getInstance().getReference().child("cases/" + caseId + "/maps")
                .listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        mapItems.addAll(listResult.getItems());
                        mapAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(mapAdapter.getItemCount() - 1);

                        recyclerView.setVisibility(View.VISIBLE);
                        findViewById(R.id.pbLoadingMaps).setVisibility(View.GONE);

                        if(mapItems.size() == 0) {
                            findViewById(R.id.tvNoMapExist).setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void enableAddMapButton() {
        btnAddMap = findViewById(R.id.btnAddMap);
        btnAddMap.setOnClickListener(view1 -> {
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_add_map);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            etMapsLink = dialog.findViewById(R.id.etMapLink);
            etDescription = dialog.findViewById(R.id.etDescription);
            btnUpload = dialog.findViewById(R.id.btnSelectMedia);
            loadingButton = dialog.findViewById(R.id.btnUpload);

            dialog.show();

            etMapsLink.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String link = editable.toString();

                    if(loadingButton.isLocked() || mapUri != null) {
                        return;
                    }

                    if(link.matches(mapLinkRegex)) {
                        loadingButton.setEnabled();
                    } else {
                        loadingButton.setDisabled();
                    }
                }
            });

            btnUpload.setOnClickListener(v -> mGetContent.launch(new Intent(Intent.ACTION_PICK).setType("image/*")));
            dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
            dialog.setOnDismissListener(dialogInterface -> mapUri = null);

            loadingButton.setOnClickListener(v -> {
                if(loadingButton.isInProgress()) {
                    return;
                }

                etDescription.setEnabled(false);

                loadingButton.start();
                uploadMap();
            });
        });
    }

    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        mapUri = result.getData().getData();

                        Drawable drawable = getResources().getDrawable(R.drawable.icon_file_download_done);
                        drawable.setTint(Color.WHITE);
                        btnUpload.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                        btnUpload.setText("נבחרה מפה");
                        btnUpload.setEnabled(false);
                        btnUpload.setAlpha(0.5f);

                        loadingButton.setEnabled();
                    } else if(result.getResultCode() != RESULT_CANCELED) {
                        new ErrorDialog(
                                CaseMapsActivity.this,
                                "סיבה לא ידועה",
                                "אירעה שגיאה לא ידועה במהלך העלאת המפה. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                "סגירה"
                        ).show();
                    }
                }
            });

    private void uploadMap() {
        String type = ".link";
        if(mapUri != null) {
            type = ".jpeg";
        }

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("cases/" + caseId + "/maps/" + System.currentTimeMillis() + type);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("userId", Database.userId)
                .setCustomMetadata("description", etDescription.getText().toString().trim().matches("[\\s_]+") ? "" : etDescription.getText().toString().trim())
                .setCustomMetadata("mapLink", etMapsLink.getText().toString().trim().matches(mapLinkRegex) ? etMapsLink.getText().toString().trim() : "")
                .build();

        if(aCase.isActive() && aCase.isUserInCase(Database.userId)) {
            if(mapUri != null) {
                fileRef.putFile(mapUri, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        loadingButton.stop();
                        dialog.dismiss();

                        updateMapList();
                    }
                });
            } else {
                fileRef.putStream(new ByteArrayInputStream(new byte[0]), metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        loadingButton.stop();
                        dialog.dismiss();

                        updateMapList();
                    }
                });
            }
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
    public void onMapClick(Uri uri, boolean isLink) {
        Dialog customDialog = new Dialog(CaseMapsActivity.this);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_map);
        customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        customDialog.findViewById(R.id.btnClose).setOnClickListener(v -> customDialog.dismiss());

        ImageView imageView = customDialog.findViewById(R.id.ivImage);

        if(isLink) {
            imageView.setVisibility(View.GONE);
            ((TextView) customDialog.findViewById(R.id.tvTitle)).setText("לחץ על הסרטון כדי להפעיל");
        } else {
            Glide.with(CaseMapsActivity.this).load(uri).into(imageView);
            imageView.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, uri).setDataAndType(uri, "image/*")));
        }

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

                        AppCompatButton btnMapLink = customDialog.findViewById(R.id.btnMapLink);
                        String mapLink = storageMetadata.getCustomMetadata("mapLink");
                        if(mapLink != null && !mapLink.isEmpty()) {
                            btnMapLink.setVisibility(View.VISIBLE);
                            btnMapLink.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mapLink))));
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

        deleteMap(customDialog, uri);

        customDialog.show();
    }

    private void deleteMap(Dialog customDialog, Uri uri) {
        ImageButton btnDelete = customDialog.findViewById(R.id.btnDelete);
        if(!aCase.isActive()) {
            btnDelete.setVisibility(View.GONE);
        }

        if(Database.user != null && Database.user.getRole() > 1 && aCase.isUserInCase(Database.userId) && aCase.isActive()) {
            btnDelete.setVisibility(View.VISIBLE);

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ConfirmDialog(
                            CaseMapsActivity.this,
                            "מחק מפה",
                            "האם את/ה בטוח/ה שאת/ה רוצה למחוק את המפה?",
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

                                            updateMapList();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            new ErrorDialog(
                                                    CaseMapsActivity.this,
                                                    "מחיקה לא צלחה",
                                                    "מסיבה לא ידועה מחיקת המפה לא צלחה. נסה שוב בשביל למחוק.",
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
        btnAddMap.setVisibility(aCase.isActive() && aCase.isUserInCase(Database.userId) ? View.VISIBLE : View.GONE);
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