package com.example.findme.home.users;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.classes.dialogs.ConfirmClickListener;
import com.example.findme.classes.dialogs.ConfirmDialog;
import com.example.findme.classes.dialogs.ErrorDialog;
import com.example.findme.classes.ui.DownSelect;
import com.example.findme.classes.ui.SearchEditText;
import com.example.findme.classes.users.User;
import com.example.findme.classes.users.UserAdapter;
import com.example.findme.home.users.createUser.CreateUserActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment implements Database.CollectionUpdateListener, UserAdapter.OnUserClickListener {
    View view;
    List<User> userList = new ArrayList<>();
    List<String> userListId = new ArrayList<>();
    UserAdapter adapter;
    FloatingActionButton btnCreateUser;
    SearchEditText etSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_users, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserAdapter(userList, userListId, requireContext(), this);
        recyclerView.setAdapter(adapter);

        btnCreateUser = view.findViewById(R.id.btnCreateUser);
        btnCreateUser.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), CreateUserActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.setFilter(editable.toString());
                adapter.notifyDataSetChanged();
            }

        });

        Database.listenForCollectionUpdates("users", this);


        return view;
    }

    private void updateUserList() {
        userList.clear();
        userListId.clear();

        FirebaseFirestore.getInstance().collection("users")
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            User user = document.toObject(User.class);

                            userList.add(user);
                            userListId.add(document.getId());
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onCollectionUpdated() {
        updateUserList();
    }

    @Override
    public void onUserClick(User user, String userId) {
        if(user.getRole() > Database.user.getRole() - 1) {
            new ErrorDialog(
                    requireContext(),
                    "אין גישה",
                    "משתמש יקר, אין לך גישה בשביל לערוך את משתמש זה.",
                    "סגור"
            ).show();

            return;
        }

        Dialog customDialog = new Dialog(requireContext());
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_user);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        customDialog.findViewById(R.id.btnClose).setOnClickListener(v -> customDialog.dismiss());

        DownSelect downSelect = customDialog.findViewById(R.id.downSelect);

        if(user.getRole() == 3 || Database.user.getRole() == 2) {
            downSelect.setVisibility(View.GONE);
            customDialog.findViewById(R.id.tvDownSelect).setVisibility(View.GONE);
        } else {
            downSelect.setSelection(user.getRole());
            downSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if(i != user.getRole()) {
                        new ConfirmDialog(
                                requireContext(),
                                "שינוי סוג משתמש",
                                "האם את/ה בטוח/ה שאת/ה רוצה לשנות את סוג המשתמש של " + user.getFirstName() + " " + user.getLastName() + "?",
                                "שינוי",
                                "ביטול",
                                new ConfirmClickListener() {
                                    @Override
                                    public void onOkClicked() {
                                        if(!Database.isNetworkConnected(requireContext())) {
                                            new ErrorDialog(
                                                    requireContext(),
                                                    "בעיה ברשת",
                                                    "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                                    "חזור"
                                            ).show();

                                            return;
                                        }

                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(userId)
                                                .update("role", i)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        new ErrorDialog(
                                                                requireContext(),
                                                                "סוג משתמש שונה",
                                                                "משתמש יקר, סוג המשתמש של " + user.getFirstName() + " " + user.getLastName() + " שונה בהצלחה.",
                                                                "סגור"
                                                        ).show();

                                                        customDialog.dismiss();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        new ErrorDialog(
                                                                requireContext(),
                                                                "סיבה לא ידועה",
                                                                "אירעה שגיאה לא ידועה במהלך שינוי סוג המשתמש. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                                "חזור"
                                                        ).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelClick() {
                                        downSelect.setSelection(user.getRole());
                                    }
                                }
                        ).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        ((TextView) customDialog.findViewById(R.id.tvUserName)).setText(user.getFirstName() + " " + user.getLastName());

        FirebaseStorage.getInstance().getReference("users").child(userId)
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(requireContext()).load(uri).into(((ImageView) customDialog.findViewById(R.id.ivUserImage)));

                    }
                });

        if(user.getLoginAttempts() == Integer.parseInt(getString(R.string.login_attempts))) {
            ((ImageView) customDialog.findViewById(R.id.ivLock)).setImageResource(R.drawable.icon_unlock);
            ((TextView) customDialog.findViewById(R.id.tvLock)).setText("הסר נעילת משתמש");

            customDialog.findViewById(R.id.btnLockUser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ConfirmDialog(
                            requireContext(),
                            "הסר נעילת משתמש",
                            "האם את/ה בטוח/ה שאת/ה רוצה להסיר נעילה מהמשתמש של " + user.getFirstName() + " " + user.getLastName() + "?",
                            "הסר נעילה",
                            "ביטול",
                            new ConfirmClickListener() {
                            @Override
                            public void onOkClicked() {
                                if(!Database.isNetworkConnected(requireContext())) {
                                    new ErrorDialog(
                                            requireContext(),
                                            "בעיה ברשת",
                                            "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                            "חזור"
                                    ).show();

                                    return;
                                }

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .update("loginAttempts", 0)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                new ErrorDialog(
                                                        requireContext(),
                                                        "המשתמש הופעל",
                                                        "משתמש יקר, המשתמש של " + user.getFirstName() + " " + user.getLastName() + " הופעל מחדש.",
                                                        "סגור"
                                                ).show();

                                                customDialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                new ErrorDialog(
                                                        requireContext(),
                                                        "סיבה לא ידועה",
                                                        "אירעה שגיאה לא ידועה במהלך הפעלת המשתמש. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                        "חזור"
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
        } else {
            customDialog.findViewById(R.id.btnLockUser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ConfirmDialog(
                            requireContext(),
                            "נעל משתמש",
                            "האם את/ה בטוח/ה שאת/ה רוצה לנעול את המשתמש של " + user.getFirstName() + " " + user.getLastName() + "?",
                            "הסר נעילה",
                            "ביטול",
                            new ConfirmClickListener() {
                                @Override
                                public void onOkClicked() {
                                    if(!Database.isNetworkConnected(requireContext())) {
                                        new ErrorDialog(
                                                requireContext(),
                                                "בעיה ברשת",
                                                "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה שוב.",
                                                "חזור"
                                        ).show();

                                        return;
                                    }

                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(userId)
                                            .update("loginAttempts", Integer.parseInt(getString(R.string.login_attempts)))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    new ErrorDialog(
                                                            requireContext(),
                                                            "המשתמש ננעל",
                                                            "משתמש יקר, המשתמש של " + user.getFirstName() + " " + user.getLastName() + " ננעל בהצלחה.",
                                                            "סגור"
                                                    ).show();

                                                    customDialog.dismiss();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    new ErrorDialog(
                                                            requireContext(),
                                                            "סיבה לא ידועה",
                                                            "אירעה שגיאה לא ידועה במהלך נעילת המשתמש. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                            "חזור"
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

            customDialog.findViewById(R.id.btnDeleteUser).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new ConfirmDialog(
                            requireContext(),
                            "מחיקת משתמש",
                            "האם את/ה בטוח/ה שאת/ה רוצה למחוק את המשתמש של " + user.getFirstName() + " " + user.getLastName() + "?",
                            "מחיקה",
                            "ביטול",
                            new ConfirmClickListener() {
                                @Override
                                public void onOkClicked() {
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(userId)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    new ErrorDialog(
                                                            requireContext(),
                                                            "המשתמש נמחק",
                                                            "משתמש יקר, המשתמש של " + user.getFirstName() + " " + user.getLastName() + " נמחק בהצלחה.",
                                                            "סגור"
                                                    ).show();

                                                    customDialog.dismiss();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    new ErrorDialog(
                                                            requireContext(),
                                                            "סיבה לא ידועה",
                                                            "אירעה שגיאה לא ידועה במהלך מחיקת המשתמש. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                                                            "חזור"
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



        customDialog.show();
    }
}