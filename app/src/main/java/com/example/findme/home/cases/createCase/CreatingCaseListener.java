package com.example.findme.home.cases.createCase;

import android.content.Intent;

interface CreatingCaseListener {
    void onCaseCreationResult(Intent data);
    void onBackPressed(boolean isCaseTaken);
}
