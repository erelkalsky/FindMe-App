package com.example.findme.home.users.createUser;

import android.content.Intent;


interface CreatingUserListener {
    void onUserCreationResult(Intent data);
    void onBackPressed(boolean isPhoneTaken);
}

