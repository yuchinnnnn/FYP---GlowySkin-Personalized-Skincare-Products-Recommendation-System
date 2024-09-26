package com.example.personalizedskincareproductsrecommendation;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminViewUserProfile extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";  // Using this consistently
    private TextView usernameTextView, emailTextView, ageTextView, statusTextView, skinQuizResultsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_user_profile);

        // Retrieve the user ID from the intent
        userId = getIntent().getStringExtra(ARG_USER_ID);
        Log.d("AdminViewUserProfile", "Received userId: " + userId);  // Log for debugging

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        ageTextView = findViewById(R.id.age);
        statusTextView = findViewById(R.id.status_text);
        skinQuizResultsTextView = findViewById(R.id.skin_quiz);

        // Fetch and display the user details
        fetchUserDetails(userId);
    }

    private void fetchUserDetails(String userId) {
        // Reference to the "Users" node to fetch user details
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Assuming you have a Users class that matches your database structure
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null) {
                    // Update the UI with the user details
                    usernameTextView.setText(user.getUsername());
                    emailTextView.setText(user.getEmail());
                    ageTextView.setText(String.valueOf(user.getAge()));
                    statusTextView.setText("Status: " + user.getStatus());
                    skinQuizResultsTextView.setText(user.getSkinQuizResults());
                } else {
                    Toast.makeText(AdminViewUserProfile.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(AdminViewUserProfile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
