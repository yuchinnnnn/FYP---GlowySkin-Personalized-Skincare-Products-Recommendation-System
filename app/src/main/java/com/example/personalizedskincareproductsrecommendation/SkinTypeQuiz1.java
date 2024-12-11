package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinTypeQuiz1 extends AppCompatActivity {

    private LinearLayout dry, normal, combination, oil;
    private ImageView next, back;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private String selectedSkinType;
    private String existingSkinType;
    private boolean isLoadedFromDatabase = false; // New flag

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_SELECTED_SKIN_TYPE = "selectedSkinType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type_quiz1);

        dry = findViewById(R.id.dry);
        normal = findViewById(R.id.normal);
        combination = findViewById(R.id.combination);
        oil = findViewById(R.id.oil);
        next = findViewById(R.id.next);
        back = findViewById(R.id.back_button);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId).child("skinTypeQuiz1");

        // Load the existing skin type from Firebase
        loadExistingSkinType();

        dry.setOnClickListener(v -> selectSkinType("Dry", dry));
        normal.setOnClickListener(v -> selectSkinType("Normal", normal));
        combination.setOnClickListener(v -> selectSkinType("Combination", combination));
        oil.setOnClickListener(v -> selectSkinType("Oil", oil));

        next.setOnClickListener(v -> {
            if (selectedSkinType != null) { // If user selected a new skin type
                if (existingSkinType != null && !selectedSkinType.equals(existingSkinType)) {
                    new SweetAlertDialog(SkinTypeQuiz1.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Change")
                            .setContentText("Are you sure you want to change your answer?")
                            .setConfirmText("Yes!")
                            .setCancelText("No")
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                updateSkinTypeInDatabase();
                                Toast.makeText(SkinTypeQuiz1.this, "Skin type updated successfully", Toast.LENGTH_SHORT).show();
                            })
                            .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                } else {
                    updateSkinTypeInDatabase();
                }
            } else if (existingSkinType != null) { // If user didn't make changes but has an existing answer
                Intent intent = new Intent(SkinTypeQuiz1.this, SkinTypeQuiz2.class);
                intent.putExtra(ARG_USER_ID, userId);
                intent.putExtra(ARG_SELECTED_SKIN_TYPE, existingSkinType); // Pass existing answer
                Toast.makeText(SkinTypeQuiz1.this, "Answer saved", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                Toast.makeText(SkinTypeQuiz1.this, "Please select an answer", Toast.LENGTH_SHORT).show(); // No selection and no existing answer
            }
        });

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .addToBackStack(null) // Adds the transaction to the back stack
                    .commit();
        });
    }

    private void loadExistingSkinType() {
        isLoadedFromDatabase = true; // Set the flag before loading data
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                existingSkinType = dataSnapshot.getValue(String.class);
                if (existingSkinType != null) {
                    // Highlight the previously selected skin type
                    switch (existingSkinType) {
                        case "Dry":
                            highlightSkinType(dry);
                            break;
                        case "Normal":
                            highlightSkinType(normal);
                            break;
                        case "Combination":
                            highlightSkinType(combination);
                            break;
                        case "Oil":
                            highlightSkinType(oil);
                            break;
                    }
                }
                isLoadedFromDatabase = false; // Reset the flag after data is loaded
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz1.this, "Failed to load existing answer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectSkinType(String skinType, LinearLayout selectedLayout) {
        if (!isLoadedFromDatabase) {  // Only update selectedSkinType if the user made a selection
            selectedSkinType = skinType;
        }
        highlightSkinType(selectedLayout);
    }

    private void highlightSkinType(LinearLayout selectedLayout) {
        // Set all options to unselected
        dry.setSelected(false);
        normal.setSelected(false);
        combination.setSelected(false);
        oil.setSelected(false);

        // Set the background resource for each option
        dry.setBackgroundResource(R.drawable.combination_background);
        normal.setBackgroundResource(R.drawable.combination_background);
        combination.setBackgroundResource(R.drawable.combination_background);
        oil.setBackgroundResource(R.drawable.combination_background);

        // Highlight the selected layout
        selectedLayout.setSelected(true);
    }

    private void updateSkinTypeInDatabase() {
        databaseReference.setValue(selectedSkinType)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SkinTypeQuiz1.this, SkinTypeQuiz2.class);
                        intent.putExtra(ARG_USER_ID, userId);
                        intent.putExtra(ARG_SELECTED_SKIN_TYPE, selectedSkinType);
                        startActivity(intent);
                        Toast.makeText(SkinTypeQuiz1.this, "Answer saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SkinTypeQuiz1.this, "Failed to save answer", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

