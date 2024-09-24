package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class AdminViewFeedback extends AppCompatActivity {

    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_feedback);
    }
}