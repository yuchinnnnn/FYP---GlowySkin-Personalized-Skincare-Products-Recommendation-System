package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminViewUserProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";  // Using this consistently
    private TextView usernameTextView, emailTextView, ageTextView, statusTextView, skinQuizResultsTextView;
    private CircleImageView profilePic;
    private ListView skinAnalysisListView;
    private ImageView back;

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

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminViewUserProfile.this, AdminManageProfile.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        profilePic = findViewById(R.id.profile_pic);
        loadProfilePhoto(userId);

        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        ageTextView = findViewById(R.id.age);
        statusTextView = findViewById(R.id.status);
        skinQuizResultsTextView = findViewById(R.id.skin_quiz);
        skinAnalysisListView = findViewById(R.id.user_skin_analysis_list);

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
                    statusTextView.setText(user.getStatus());

                    fetchSkinQuizResults(userId);
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

    private void fetchSkinQuizResults(String userId) {
        // Reference to the "SkinQuiz" node to fetch skin quiz results
        DatabaseReference skinQuizResultsRef = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId);

        skinQuizResultsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve individual fields from the snapshot
                    String result = dataSnapshot.child("result").getValue(String.class);
//                    String skinTypeQuiz1 = dataSnapshot.child("skinTypeQuiz1").getValue(String.class);
//                    String skinTypeQuiz2 = dataSnapshot.child("skinTypeQuiz2").getValue(String.class);
//                    String skinTypeQuiz3 = dataSnapshot.child("skinTypeQuiz3").getValue(String.class);
//                    String skinTypeQuiz4 = dataSnapshot.child("skinTypeQuiz4").getValue(String.class);

                    // Update the UI with the retrieved values
                    if (result != null) {
                        skinQuizResultsTextView.setText(result);
                    } else {
                        skinQuizResultsTextView.setText("No result found");
                    }

//                    // You can display the other fields similarly in other TextViews or a single TextView
//                    skinTypeQuiz1TextView.setText("Skin Type 1: " + (skinTypeQuiz1 != null ? skinTypeQuiz1 : "N/A"));
//                    skinTypeQuiz2TextView.setText("Skin Type 2: " + (skinTypeQuiz2 != null ? skinTypeQuiz2 : "N/A"));
//                    skinTypeQuiz3TextView.setText("Skin Type 3: " + (skinTypeQuiz3 != null ? skinTypeQuiz3 : "N/A"));
//                    skinTypeQuiz4TextView.setText("Skin Type 4: " + (skinTypeQuiz4 != null ? skinTypeQuiz4 : "N/A"));

                } else {
                    skinQuizResultsTextView.setText("No quiz results found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminViewUserProfile.this, "Failed to load quiz results", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadProfilePhoto(String userId) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference profileImageRef = userReference.child("profilePhotoUrl");

        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the profilePhotoUrl exists
                if (snapshot.exists()) {
                    String profilePhotoUrl = snapshot.getValue(String.class);
                    if (profilePhotoUrl != null) {
                        // Load the image using the URL with Glide
                        Glide.with(AdminViewUserProfile.this).load(profilePhotoUrl).into(profilePic);
                        Log.d(TAG, "Profile photo loaded successfully.");
                    } else {
                        Log.d(TAG, "Profile photo URL is null.");
                        // Load a default image if profile photo URL is null
                        profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile photo URL: " + error.getMessage());
                // Load a default image in case of error
                profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
            }
        });
    }
}
