package com.example.findme.home.settings;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.home.settings.editUser.EditUserActivity;
import com.example.findme.home.settings.editUser.EditUserPasswordActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

public class SettingsFragment extends Fragment {
    View view;
    ErrorDialog errorDialog;
    Button btnEditProfilePic;
    FrameLayout btnLogout, btnFirstName, btnLastName, btnPhone, btnPassword;
    TextView tvFirstName, tvLastName, tvPhone, tvRole, tvCasesIn;
    ImageView ivUserImage;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        findViews();
        updateUi();

        return view;
    }

    private void findViews() {
        ivUserImage = view.findViewById(R.id.ivUserImage);
        tvFirstName = view.findViewById(R.id.tvFirstName);
        tvLastName = view.findViewById(R.id.tvLastName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvRole = view.findViewById(R.id.tvRole);
        tvCasesIn = view.findViewById(R.id.tvCasesIn);

        btnEditProfilePic = view.findViewById(R.id.btnEditProfilePic);
        btnEditProfilePic.setOnClickListener(view -> openImageDialog());

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> handleLogout());

        btnFirstName = view.findViewById(R.id.btnFirstName);
        btnFirstName.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), EditUserActivity.class).putExtra("edit", "firstName"));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnLastName = view.findViewById(R.id.btnLastName);
        btnLastName.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), EditUserActivity.class).putExtra("edit", "lastName"));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnPhone = view.findViewById(R.id.btnPhone);
        btnPhone.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), EditUserActivity.class).putExtra("edit", "phone"));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnPassword = view.findViewById(R.id.btnPassword);
        btnPassword.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), EditUserPasswordActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    public void updateUi() {
        FirebaseStorage.getInstance().getReference("users").child(Database.userId)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(isVisible()) {
                            Glide.with(requireContext()).load(uri).into(ivUserImage);
                        }
                    }
                });

        tvFirstName.setText(Database.user.getFirstName());
        tvLastName.setText(Database.user.getLastName());
        tvPhone.setText(Database.user.getPhone());
        tvRole.setText(Database.roleToString(getContext(), Database.user.getRole()));

        FirebaseFirestore.getInstance().collection("cases")
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int counter = 0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Case aCase = document.toObject(Case.class);

                            if(aCase.isActive() && aCase.isUserInCase(Database.userId)) {
                                counter++;
                            }
                        }

                        tvCasesIn.setText("" + counter);
                    }
                });
    }

    private void openImageDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_edit_user_image);
        dialog.show();

        dialog.findViewById(R.id.btnChooseFromGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btnChooseFromCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmDialog confirmDialog = new ConfirmDialog(
                        getActivity(),
                        "מחיקת תמונה פרופיל",
                        "האם את/ה בטוח/ה שאת/ה רוצה למחוק את תמונה הפרופיל",
                        "מחק",
                        "ביטול",
                        new ConfirmClickListener() {
                            @Override
                            public void onOkClicked() {
                                FirebaseStorage.getInstance().getReference("users").child(Database.userId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                ivUserImage.setImageDrawable(getActivity().getDrawable(R.drawable.img_default_profile));
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                errorDialog = new ErrorDialog(
                                                        getActivity(),
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

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "FindMe", null);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());
        return Uri.parse(path);
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
                    .start(requireContext(), this);
        }
        else if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            Uri selectedImageUri = getImageUriFromBitmap(imageBitmap);

            CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setCropMenuCropButtonTitle("סיום")
                    .start(requireContext(), this);
        }
        else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();

                FirebaseStorage.getInstance().getReference("users").child(Database.userId).putFile(croppedImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ivUserImage.setImageURI(croppedImageUri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                errorDialog = new ErrorDialog(
                                        getActivity(),
                                        "סיבה לא ידועה",
                                        "אירעה שגיאה לא ידועה במהלך החלפת התמונה. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                        "חזור"
                                );
                                errorDialog.show();
                            }
                        });
            }
        }
    }
    private void handleLogout() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.dialog_logout);
        dialog.show();

        LinearLayout btnLogout = dialog.findViewById(R.id.btnLogout),
                btnCancel = dialog.findViewById(R.id.btnCancel);

        btnLogout.setOnClickListener(view -> Database.logout(getActivity()));
        btnCancel.setOnClickListener(view -> dialog.dismiss());
    }
}