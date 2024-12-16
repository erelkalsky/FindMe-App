package com.example.findme.classes.ui;

import android.text.Editable;
import android.text.TextWatcher;

import com.example.findme.classes.api.Database;

public abstract class EditTextListener { // abstract = מחלקה שאי אפשר לירוש ממנה

    public static TextWatcher phone(ErrorEditText etPhone, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = editable.toString();

                if(phone.isEmpty()) {
                    btnSave.setDisabled();
                    etPhone.removeError();
                } else if(phone.equals(Database.user.getPhone())) {
                    btnSave.setDisabled();
                    etPhone.setError("הטלפון הנתון הוא הטלפון של משתמש זה.");
                } else if (isValidPhone(phone)) {
                    btnSave.setEnabled();
                    etPhone.removeError();
                } else {
                    btnSave.setDisabled();
                    etPhone.setError("מספר טלפון אינו תקין.");
                }
            }
        };
    }

    private static boolean isValidPhone(String phone) {
        //String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; //valid email
        String regex = "05[0-9]{8}"; //valid phone number israel
        return phone.matches(regex);
    }

    public static TextWatcher firstName(ErrorEditText etFirstName, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(text.isEmpty()) {
                    btnSave.setDisabled();
                    etFirstName.removeError();
                } else if(text.length() < 2) {
                    btnSave.setDisabled();
                    etFirstName.setError("שם פרטי צריך להיות לפחות עם שתי אותיות.");
                } else {
                    btnSave.setEnabled();
                    etFirstName.removeError();
                }
            }
        };
    }

    public static TextWatcher lastName(ErrorEditText etLastName, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(text.isEmpty()) {
                    btnSave.setDisabled();
                    etLastName.removeError();
                } else if(text.length() < 2) {
                    btnSave.setDisabled();
                    etLastName.setError("שם משפחה צריך להיות לפחות עם שתי אותיות.");
                } else {
                    btnSave.setEnabled();
                    etLastName.removeError();
                }
            }
        };
    }

    public static TextWatcher equalToBefore(String before, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String after = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(after.equals(before)) {
                    btnSave.setDisabled();
                }
            }
        };
    }

    public static TextWatcher caseId(ErrorEditText etCase, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(text.isEmpty()) {
                    btnSave.setDisabled();
                    etCase.removeError();
                } else if(text.length() < 4) {
                    btnSave.setDisabled();
                    etCase.setError("מספר תיק צריך להיות לפחות עם 4 ספרות.");
                } else {
                    btnSave.setEnabled();
                    etCase.removeError();
                }
            }
        };
    }

    public static TextWatcher personId(ErrorEditText etPersonId, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(text.isEmpty()) {
                    btnSave.setDisabled();
                    etPersonId.removeError();
                } else if(!isIsraeliIdNumber(text)) {
                    btnSave.setDisabled();
                    etPersonId.setError("תעודת זהות אינה תקינה.");
                } else {
                    btnSave.setEnabled();
                    etPersonId.removeError();
                }
            }

            private boolean isIsraeliIdNumber(String id) {
                id = id.trim();
                if (id.length() != 9 || !id.matches("\\d+")) return false;
                int sum = 0;
                for (int i = 0; i < id.length(); i++) {
                    int digit = Character.getNumericValue(id.charAt(i));
                    int step = digit * ((i % 2) == 0 ? 1 : 2); // Double every other digit
                    sum += step > 9 ? step - 9 : step;
                }
                return sum % 10 == 0;
            }
        };
    }

    public static TextWatcher bigText(String before, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String after = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(after.equals(before) || before == null && after.isEmpty() || after.matches("[\\s_]+") && !after.isEmpty()) {
                    btnSave.setDisabled();
                } else {
                    btnSave.setEnabled();
                }
            }
        };
    }

    public static TextWatcher validPhone(ErrorEditText etPhone, String before, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String phone = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(before == null && phone.isEmpty()) {
                    btnSave.setDisabled();
                    etPhone.removeError();
                } else if(phone.isEmpty()) {
                    btnSave.setEnabled();
                    etPhone.removeError();
                } else {
                    if(isValidPhone(phone)) {
                        btnSave.setEnabled();
                        etPhone.removeError();
                    } else {
                        btnSave.setDisabled();
                        etPhone.setError("מספר טלפון אינו תקין.");
                    }
                }
            }
        };
    }

    public static TextWatcher validAge(String before, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String age = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(age.equals(before) || before == null && age.isEmpty() || age.startsWith("0") || !age.isEmpty() && (Integer.parseInt(age) > 120)) {
                    btnSave.setDisabled();
                } else {
                    btnSave.setEnabled();
                }
            }
        };
    }

    public static TextWatcher validHeight(String before, LoadingButton btnSave) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String height = editable.toString();

                if(btnSave.isLocked()) {
                    return;
                }

                if(height.equals(before) || before == null && height.isEmpty() || height.startsWith("0") || !height.isEmpty() && (Integer.parseInt(height) > 250)) {
                    btnSave.setDisabled();
                } else {
                    btnSave.setEnabled();
                }
            }
        };
    }
}
