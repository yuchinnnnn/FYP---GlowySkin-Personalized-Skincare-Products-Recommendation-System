package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinTypeQuiz4 extends AppCompatActivity {
    LinearLayout travel, makeup, outdoor, humid, urban;
    private ImageView previous, back;
    private Button save;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private String selectedEnvironment;
    private static final String ARG_USER_ID = "userId";

    // Variables to store answers from previous quizzes
    private String answerQuiz1, answerQuiz2, answerQuiz3, existingEnvironment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type_quiz4);

        travel = findViewById(R.id.travel);
        makeup = findViewById(R.id.makeup);
        outdoor = findViewById(R.id.outdoor);
        humid = findViewById(R.id.humid);
        urban = findViewById(R.id.urban);
        previous = findViewById(R.id.previous);
        save = findViewById(R.id.save_button);
        back = findViewById(R.id.back_button);

        userId = getIntent().getStringExtra(ARG_USER_ID);
        Log.d("SkinTypeQuiz4", "userId: " + userId); // Log the userId

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId);

        // Load data from previous quizzes
        loadPreviousQuizData();
        // Load existing answer for quiz 4
        loadExistingReaction();

        travel.setOnClickListener(v -> selectEnvironment("Travel", travel));
        makeup.setOnClickListener(v -> selectEnvironment("Makeup", makeup));
        outdoor.setOnClickListener(v -> selectEnvironment("Outdoor", outdoor));
        humid.setOnClickListener(v -> selectEnvironment("Humid", humid));
        urban.setOnClickListener(v -> selectEnvironment("Urban", urban));

        previous.setOnClickListener(v -> {
            Intent intent = new Intent(SkinTypeQuiz4.this, SkinTypeQuiz3.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });

        save.setOnClickListener(v -> {
            if (selectedEnvironment != null) {
                // Save the selected environment to the database
                databaseReference.child("skinTypeQuiz4").setValue(selectedEnvironment)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Analyze and show the skin type dialog
                                String skinType = analyzeSkinType();
                                showSkinTypeDialog(skinType); // Show the dialog
                            } else {
                                new SweetAlertDialog(SkinTypeQuiz4.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("Failed to save your answer.")
                                        .show();
                            }
                        });
            } else {
                new SweetAlertDialog(SkinTypeQuiz4.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Hold on")
                        .setContentText("Please select an answer before proceeding.")
                        .show();
            }
        });
    }

    private void loadPreviousQuizData() {
        databaseReference.child("skinTypeQuiz1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                if (value instanceof String) {
                    answerQuiz1 = (String) value;
                } else if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof String) {
                        answerQuiz1 = (String) list.get(0);
                    }
                }
                Log.d("SkinTypeQuiz4", "answerQuiz1: " + answerQuiz1);

                databaseReference.child("skinTypeQuiz2").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object value = dataSnapshot.getValue();
                        if (value instanceof String) {
                            answerQuiz2 = (String) value;
                        } else if (value instanceof List) {
                            List<?> list = (List<?>) value;
                            if (!list.isEmpty() && list.get(0) instanceof String) {
                                answerQuiz2 = (String) list.get(0);
                            }
                        }
                        Log.d("SkinTypeQuiz4", "answerQuiz2: " + answerQuiz2);

                        databaseReference.child("skinTypeQuiz3").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Object value = dataSnapshot.getValue();
                                if (value instanceof String) {
                                    answerQuiz3 = (String) value;
                                } else if (value instanceof List) {
                                    List<?> list = (List<?>) value;
                                    if (!list.isEmpty() && list.get(0) instanceof String) {
                                        answerQuiz3 = (String) list.get(0);
                                    }
                                }
                                Log.d("SkinTypeQuiz4", "answerQuiz3: " + answerQuiz3);

                                // Enable save button after data is loaded
                                save.setEnabled(true);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(SkinTypeQuiz4.this, "Failed to load Quiz 3 data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SkinTypeQuiz4.this, "Failed to load Quiz 2 data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz4.this, "Failed to load Quiz 1 data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingReaction() {
        databaseReference.child("skinTypeQuiz4").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                existingEnvironment = dataSnapshot.getValue(String.class);
                // Update UI based on existing answer
                if (existingEnvironment != null) {
                    selectEnvironment(existingEnvironment, getLayoutByAnswer(existingEnvironment));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz4.this, "Failed to load existing answer", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private LinearLayout getLayoutByAnswer(String answer) {
        switch (answer) {
            case "Travel":
                return travel;
            case "Makeup":
                return makeup;
            case "Outdoor":
                return outdoor;
            case "Humid":
                return humid;
            case "Urban":
                return urban;
            default:
                return null; // Return null if no matching layout found
        }
    }

    private void selectEnvironment(String answer, LinearLayout selectedLayout) {
        travel.setSelected(false);
        makeup.setSelected(false);
        outdoor.setSelected(false);
        humid.setSelected(false);
        urban.setSelected(false);

        travel.setBackgroundResource(R.drawable.combination_background);
        makeup.setBackgroundResource(R.drawable.combination_background);
        outdoor.setBackgroundResource(R.drawable.combination_background);
        humid.setBackgroundResource(R.drawable.combination_background);
        urban.setBackgroundResource(R.drawable.combination_background);

        selectedLayout.setSelected(true);
        selectedEnvironment = answer;
    }

    private String analyzeSkinType() {
        boolean isSensitive = !"None".equals(answerQuiz2) && "Often".equals(answerQuiz3);

        switch (answerQuiz1) {
            case "Dry":
                return isSensitive ? "Dry and Sensitive Skin" : "Dry Skin";
            case "Oil":
                return isSensitive && "Rare".equals(answerQuiz3) ? "Oily and Sensitive Skin" : "Oily Skin";
            case "Combination":
                return isSensitive && "Rare".equals(answerQuiz3) ? "Combination and Sensitive Skin" : "Combination Skin";
            default:
                return "Normal Skin";
        }
    }


    private void showSkinTypeDialog(String skinType) {
        SweetAlertDialog dialog = new SweetAlertDialog(SkinTypeQuiz4.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Your Skin Type")
                .setContentText("Based on your answers, your skin type is: " + skinType)
                .setConfirmText("OK");

        dialog.setConfirmClickListener(sweetAlertDialog -> {
            // Save the analyzed skin type and quiz answers to the database
            saveQuizResultsToDatabase(skinType);

            // Close the dialog
            sweetAlertDialog.dismiss();

            // Navigate to the MainActivity (or any activity that hosts the HomeFragment)
            navigateToHomeFragment();
        });

        dialog.show();
    }

    private void navigateToHomeFragment() {
        HomeFragment homeFragment = HomeFragment.newInstance(userId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }



    private void saveQuizResultsToDatabase(String skinType) {
        if (userId != null) {
            DatabaseReference userQuizRef = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId);

            // Save the analyzed skin type
            userQuizRef.child("result").setValue(skinType);

            // Optionally, save the answers from previous quizzes
            userQuizRef.child("skinTypeQuiz1").setValue(answerQuiz1);
            userQuizRef.child("skinTypeQuiz2").setValue(answerQuiz2);
            userQuizRef.child("skinTypeQuiz3").setValue(answerQuiz3);
            userQuizRef.child("skinTypeQuiz4").setValue(selectedEnvironment);

            Toast.makeText(SkinTypeQuiz4.this, "Quiz results saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SkinTypeQuiz4.this, "User ID is not available", Toast.LENGTH_SHORT).show();
        }
    }
}
