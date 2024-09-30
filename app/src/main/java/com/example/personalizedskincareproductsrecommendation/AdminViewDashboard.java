package com.example.personalizedskincareproductsrecommendation;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminViewDashboard extends AppCompatActivity {
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private ImageView back;
    private TextView numUsersTextView, oilySkinUsersTextView, drySkinUsersTextView, normalSkinUsersTextView, feedbackTextView;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_dashboard);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminViewDashboard.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        // Initialize TextViews
        numUsersTextView = findViewById(R.id.num_users);
        oilySkinUsersTextView = findViewById(R.id.oily_skin_users);
        drySkinUsersTextView = findViewById(R.id.dry_skin_users);
        normalSkinUsersTextView = findViewById(R.id.normal_skin_users);
        feedbackTextView = findViewById(R.id.feedbackTextView);

        // Fetch data from Firebase
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        // Get reference to your Firebase Realtime Database
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference skinTypesReference = FirebaseDatabase.getInstance().getReference("SkinQuiz");

        // Fetch user count
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int counter = (int) dataSnapshot.getChildrenCount();
                String userCounter = String.valueOf(counter);
                numUsersTextView.setText(userCounter);
                Log.d("User Counter", userCounter);

                // Now fetch skin types after counting users
                countSkinTypes(skinTypesReference, counter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminViewDashboard", "Failed to read users.", databaseError.toException());
            }
        });
    }

    private void countSkinTypes(DatabaseReference skinTypesReference, int totalUsers) {
        skinTypesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int oilyCount = 0;
                int dryCount = 0;
                int normalCount = 0;

                for (DataSnapshot skinTypeSnapshot : dataSnapshot.getChildren()) {
                    String skinType = skinTypeSnapshot.child("result").getValue(String.class);
                    if ("Oily Skin".equals(skinType)) {
                        oilyCount++;
                    } else if ("Dry Skin".equals(skinType)) {
                        dryCount++;
                    } else if ("Normal Skin".equals(skinType)) {
                        normalCount++;
                    }
                }

                // Update the UI with the skin type counts
                oilySkinUsersTextView.setText(String.valueOf(oilyCount));
                drySkinUsersTextView.setText(String.valueOf(dryCount));
                normalSkinUsersTextView.setText(String.valueOf(normalCount));
                Log.d("Skin Type Counts", "Oily: " + oilyCount + ", Dry: " + dryCount + ", Normal: " + normalCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AdminViewDashboard", "Failed to read skin types.", databaseError.toException());
            }
        });
    }
}

