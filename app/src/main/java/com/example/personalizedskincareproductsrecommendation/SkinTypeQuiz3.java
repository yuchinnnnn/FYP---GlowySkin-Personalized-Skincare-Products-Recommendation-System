package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class SkinTypeQuiz3 extends AppCompatActivity {
    LinearLayout often, rare;
    private ImageView next, previous, back;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private String selectedReaction;
    private String existingReaction;

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_SELECTED_SKIN_TYPE = "selectedSkinType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type_quiz3);

        often = findViewById(R.id.often);
        rare = findViewById(R.id.rare);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        back = findViewById(R.id.back_button);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId);

        // Load existing selected reaction from Firebase
        loadExistingReaction();

        often.setOnClickListener(v -> selectReaction("Often", often));
        rare.setOnClickListener(v -> selectReaction("Rare", rare));

        previous.setOnClickListener(v -> {
            Intent intent = new Intent(SkinTypeQuiz3.this, SkinTypeQuiz2.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        next.setOnClickListener(v -> {
            if (selectedReaction != null) {
                if (!selectedReaction.equals(existingReaction)) {
                    new SweetAlertDialog(SkinTypeQuiz3.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Change")
                            .setContentText("Are you sure you want to change your answer?")
                            .setConfirmText("Yes, change it!")
                            .setCancelText("No, keep it")
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                updateReactionInDatabase();
                            })
                            .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                } else {
                    proceedToNextQuiz();
                }
            } else {
                Toast.makeText(SkinTypeQuiz3.this, "Please select an answer", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });
    }

    private void loadExistingReaction() {
        databaseReference.child("skinTypeQuiz3").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                existingReaction = dataSnapshot.getValue(String.class);
                // Update UI based on existing answer
                updateSelection();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz3.this, "Failed to load existing answer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelection() {
        // Reset background color for all layouts
        often.setBackgroundColor(getResources().getColor(R.color.light_grey));
        rare.setBackgroundColor(getResources().getColor(R.color.light_grey));

        if ("Often".equals(existingReaction)) {
            selectReaction("Often", often);
        } else if ("Rare".equals(existingReaction)) {
            selectReaction("Rare", rare);
        }
    }

    private void selectReaction(String answer, LinearLayout selectedLayout) {
        // Reset background color for all layouts
        often.setBackgroundColor(getResources().getColor(R.color.light_grey));
        rare.setBackgroundColor(getResources().getColor(R.color.light_grey));

        // Set background color for selected layout
        selectedLayout.setBackgroundColor(getResources().getColor(R.color.pink));

        // Store the selected reaction
        selectedReaction = answer;
    }

    private void updateReactionInDatabase() {
        databaseReference.child("skinTypeQuiz3").setValue(selectedReaction)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        proceedToNextQuiz();
                    } else {
                        Toast.makeText(SkinTypeQuiz3.this, "Failed to save answer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToNextQuiz() {
        Intent intent = new Intent(SkinTypeQuiz3.this, SkinTypeQuiz4.class);
        intent.putExtra(ARG_USER_ID, userId);
        startActivity(intent);
    }

}
