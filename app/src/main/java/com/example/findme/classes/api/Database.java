package com.example.findme.classes.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.findme.R;
import com.example.findme.classes.cases.Case;
import com.example.findme.classes.users.User;
import com.example.findme.login.LoginActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {

    public static User user = null;
    public static String userId = null;

    public interface LoginListener {
        void onLoginSuccess();
        void onFirstLogin(String userId);
        void onUserLocked();
        void onUserNotFound();
        void onLoginFailure(String errorTitle, String errorMessage, String buttonText);
    }

    public static void login(Context context, String phone, String password, LoginListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("phone", phone)
                .whereEqualTo("password", password)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0); // Assuming there's only one matching document
                            User user = document.toObject(User.class);

                            if(user.isFirstLogin()) {
                                listener.onFirstLogin(document.getId());
                            } else if(user.getLoginAttempts() >= Integer.parseInt(context.getString(R.string.login_attempts))) {
                                listener.onUserLocked();
                            } else {
                                handleLoginSuccess(context, user, document.getId(), listener);
                            }
                        } else {
                            listener.onUserNotFound();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        handleLoginFailure(exception, listener);
                    }
                });
    }

    public static void login(Context context, String userId, LoginListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            handleLoginSuccess(context, user, userId, listener);
                        } else {
                            listener.onUserNotFound();
                            clearCurrentUser(context);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        handleLoginFailure(exception, listener);
                    }
                });
    }

    private static void handleLoginSuccess(Context context, User user, String userId, LoginListener listener) {
        Database.user = user;
        Database.userId = userId;

        saveCurrentUser(context, Database.userId);
        listener.onLoginSuccess();
    }

    private static void handleLoginFailure(Exception exception, LoginListener listener) {
        String[] errors = getErrorText(exception);
        listener.onLoginFailure(
                errors[0],
                errors[1],
                errors[2]
        );
    }

    private static String[] getErrorText(Exception exception) {
        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;

            switch (firestoreException.getCode()) {
                case PERMISSION_DENIED:
                    return new String[] {
                            "גישה נחסמה",
                            "נראה כי יש גישתך לשרת נחסמה. אנא בדוק את הפרטים שלך או צור קשר עם התמיכה.",
                            "חזור"
                    };

                case UNAVAILABLE:
                    return new String[] {
                            "בעיה ברשת",
                            "נראה כי יש בעיה בחיבור האינטרנט שלך. אנא בדוק את חיבור הרשת שלך ונסה להתחבר שוב.",
                            "חזור"
                    };

                default:
                    return new String[] {
                            "סיבה לא ידועה",
                            "אירעה שגיאה לא ידועה במהלך תהליך ההתחברות. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                            "חזור"
                    };
            }
        } else {
            return new String[] {
                    "סיבה לא ידועה",
                    "אירעה שגיאה לא ידועה במהלך תהליך ההתחברות. יש לנסות שוב, ואם הבעיה ממשיכה, יש לפנות לתמיכה לקבלת סיוע.",
                    "חזור"
            };
        }
    }

    private static void saveCurrentUser(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private static void clearCurrentUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("currentUser");
        editor.apply();
    }

    public static void logout(Context context) {
        Database.user = null;
        Database.userId = null;

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("currentUser");
        editor.apply();

        if(!(context instanceof LoginActivity)) {
            context.startActivity(new Intent(context, LoginActivity.class));

            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        }
    }

    public interface CreateUserListener {

        void onCreateUserSuccess();
        void onCreateUserFailure(String errorMessage);
    }

    public static void createUser(User user, CreateUserListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .document() //מיקום קישור בדאטאבייס
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        listener.onCreateUserSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onCreateUserFailure(e.getMessage());
                    }
                });
    }

    public interface UserUpdateListener {
        void onUserUpdated();
        void onUserDeleted();
    }

    public static ListenerRegistration listenForUserUpdates(UserUpdateListener listener) {
        if (Database.userId == null ) {
            return null;
        }

        return FirebaseFirestore.getInstance().collection("users")
                .document(Database.userId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot document, FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Listen failed: " + e.getMessage());
                            return;
                        }

                        if (document != null && document.exists()) {
                            User updatedUser = document.toObject(User.class);
                            Database.user = updatedUser;
                            Database.userId = document.getId();

                            listener.onUserUpdated();
                        } else {
                            listener.onUserDeleted();
                        }
                    }
                });
    }

    public interface CollectionUpdateListener {
        void onCollectionUpdated();
    }

    public static void listenForCollectionUpdates(String collection, CollectionUpdateListener listener) {
        FirebaseFirestore.getInstance().collection(collection)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            // Error handling
                            return;
                        }

                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            listener.onCollectionUpdated();
                        }
                    }
                });

    }


    public interface UpdateUserListener {
        void onUpdateUserSuccess();
        void onUpdateUserFailure();
    }

    public static void updateUser(String toUpdate, String updated, UpdateUserListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .document(Database.userId)
                .update(toUpdate, updated).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        listener.onUpdateUserSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onUpdateUserFailure();
                    }
                });
    }

    public interface IsPhoneExistListener {
        void onPhoneExist();
        void onPhoneNotExist();
        void onFailure(String errorTitle, String errorMessage, String buttonText);
    }

    public static void isPhoneExist(String phone, IsPhoneExistListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("phone", phone)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                listener.onPhoneExist();
                            }

                        } else {
                            listener.onPhoneNotExist();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String[] errors = getErrorText(e);
                        listener.onFailure(errors[0], errors[1], errors[2]);
                    }
                });
    }

    public static String roleToString(Context context, int role) {
        if (context == null) {
            return null; // or handle the null context case appropriately
        }

        Map<Integer, String> roleMap = new HashMap<Integer, String>() {{
            String roleSelection = context.getResources().getString(R.string.role_selection);
            List<String> list = Arrays.asList(roleSelection.split(",\\s*"));

            list.forEach(item -> put((list.indexOf(item) + 1), item));
        }};

        return roleMap.getOrDefault(role, null);
    }

    public interface CreateCaseListener {

        void onCreateCaseSuccess();
        void onCreateCaseFailure(String errorMessage);
    }

    public static void createCase(Case newCase, CreateCaseListener listener) {
        FirebaseFirestore.getInstance().collection("cases")
                .document() //מיקום קישור בדאטאבייס
                .set(newCase)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        listener.onCreateCaseSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onCreateCaseFailure(e.getMessage());
                    }
                });
    }

    public interface IsCaseExistListener {
        void onCaseExist();
        void onCaseNotExist();
        void onFailure(String errorTitle, String errorMessage, String buttonText);
    }

    public static void isCaseExist(String caseId, IsCaseExistListener listener) {
        FirebaseFirestore.getInstance().collection("cases")
                .whereEqualTo("caseId", caseId)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                listener.onCaseExist();
                            }

                        } else {
                            listener.onCaseNotExist();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String[] errors = getErrorText(e);
                        listener.onFailure(errors[0], errors[1], errors[2]);
                    }
                });
    }

    public interface CaseUpdateListener {
        void onCaseUpdated(Case updatedCase, String updatedCaseId);
        void onCaseDeleted();
    }

    public static ListenerRegistration listenForCaseUpdates(CaseUpdateListener listener, String caseId) {
        return FirebaseFirestore.getInstance().collection("cases")
                .document(caseId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot document, FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Listen failed: " + e.getMessage());
                            return;
                        }

                        if (document != null && document.exists()) {
                            Case updatedCase = document.toObject(Case.class);
                            listener.onCaseUpdated(updatedCase, document.getId());
                        } else {
                            // Document doesn't exist or is null
                            System.out.println("Current data: null");
                            listener.onCaseDeleted();
                        }
                    }
                });
    }

    public interface UpdateCaseListener {
        void onUpdateCaseSuccess();
        void onUpdateCaseFailure();
    }

    public static void updateCase(String caseId, String toUpdate, String updated, UpdateCaseListener listener) {
        FirebaseFirestore.getInstance().collection("cases")
                .document(caseId)
                .update(toUpdate, updated).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        listener.onUpdateCaseSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onUpdateCaseFailure();
                    }
                });
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}