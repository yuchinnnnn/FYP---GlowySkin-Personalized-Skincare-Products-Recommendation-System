package com.example.personalizedskincareproductsrecommendation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinGoals extends AppCompatActivity {

    private ChipGroup skinGoalsChipGroup;
    private Chip acne, wrinkles, spots, pores, firm, evenskin, others;
    private Button saveButton;
    private ImageView back;
    private LinearLayout othersEditTextContainer;
    private EditText othersEditText;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private List<String> skinGoals = new ArrayList<>();

    private static final String ARG_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_goals);

        skinGoalsChipGroup = findViewById(R.id.skin_goals_chip_group);
        acne = findViewById(R.id.lessen_acne);
        wrinkles = findViewById(R.id.lessen_wrinkles);
        spots = findViewById(R.id.reduce_spots);
        pores = findViewById(R.id.minimize_pores);
        firm = findViewById(R.id.firm_skin);
        evenskin = findViewById(R.id.even_skin);
        others = findViewById(R.id.others);
        saveButton = findViewById(R.id.save_button);
        back = findViewById(R.id.back_button);

        othersEditTextContainer = findViewById(R.id.others_edittext_container);
        othersEditText = findViewById(R.id.others_edittext);

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            // Handle the case where userId is not passed or retrieved
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinGoals").child(userId);

        others.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                othersEditTextContainer.setVisibility(View.VISIBLE);
            } else {
                othersEditTextContainer.setVisibility(View.GONE);
            }
        });

        saveButton.setOnClickListener(v -> saveSkinGoals());
        back.setOnClickListener(v -> {
            finish();
        });

        DatabaseReference skinGoalsRef = databaseReference.child("skinGoals");
        skinGoalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                skinGoals.clear();
                for (DataSnapshot skinGoalSnapshot : snapshot.getChildren()) {
                    String skinGoal = skinGoalSnapshot.getValue(String.class);
                    if (skinGoal != null) {
                        skinGoals.add(skinGoal);
                    }
                }
                Log.d("SkinGoals", "Skin goals: " + skinGoals);
                highlightCurrentGoals(skinGoals); // Call after the skin goals are populated
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SkinGoals.this, "Failed to load skin goals.", Toast.LENGTH_SHORT).show();
            }
        });

        setupChipListeners(); // Set up listeners for chip selection
    }

    private void setupChipListeners() {
        for (int i = 0; i < skinGoalsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) skinGoalsChipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> highlightCurrentGoals(skinGoals));
        }
    }

    private void saveSkinGoals() {
        databaseReference.child("skinGoals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> currentSkinGoals = (List<String>) task.getResult().getValue();

                StringBuilder currentGoalsText = new StringBuilder("Your current goals: ");
                if (currentSkinGoals != null && !currentSkinGoals.isEmpty()) {
                    for (String goal : currentSkinGoals) {
                        currentGoalsText.append(goal).append("\n");
                    }
                } else {
                    currentGoalsText.append("No goals set.");
                }

                List<String> selectedGoals = new ArrayList<>();
                for (int i = 0; i < skinGoalsChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) skinGoalsChipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        if (chip.getId() == R.id.others) {
                            String othersGoal = othersEditText.getText().toString();
                            if (!TextUtils.isEmpty(othersGoal)) {
                                selectedGoals.add(othersGoal);
                            }
                        } else {
                            selectedGoals.add(chip.getText().toString());
                        }
                    }
                }

                new SweetAlertDialog(SkinGoals.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you Sure?")
                        .setContentText(currentGoalsText + ". \n Do you want to change your skin goals to " + TextUtils.join(", ", selectedGoals))
                        .setConfirmText("Yes")
                        .setCancelText("No")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            Map<String, Object> skinGoalsUpdate = new HashMap<>();
                            skinGoalsUpdate.put("skinGoals", selectedGoals);

                            databaseReference.updateChildren(skinGoalsUpdate)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            new SweetAlertDialog(SkinGoals.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Saved!")
                                                    .setContentText("Your skin goals have been updated successfully.")
                                                    .setConfirmText("OK")
                                                    .setConfirmClickListener(sweetAlertDialog -> {
                                                        sweetAlertDialog.dismissWithAnimation();
                                                        finish();
                                                    })
                                                    .show();
                                        } else {
                                            new SweetAlertDialog(SkinGoals.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Failed to save skin goals. Please try again.")
                                                    .show();
                                        }
                                    });
                        })
                        .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
            } else {
                Toast.makeText(SkinGoals.this, "Failed to retrieve current skin goals. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightCurrentGoals(List<String> currentSkinGoals) {
        for (int i = 0; i < skinGoalsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) skinGoalsChipGroup.getChildAt(i);
            String chipText = chip.getText().toString();
            boolean isCurrentGoal = currentSkinGoals != null && currentSkinGoals.contains(chipText);
            boolean isSelectedGoal = chip.isChecked();

            // Highlight the chip based on current goal and selection status
            if (isSelectedGoal) {
                // Highlight color for selected goals
                chip.setChipBackgroundColorResource(R.color.black);
                chip.setTextColor(getResources().getColor(android.R.color.white)); // Text color for selected goals
            } else if (isCurrentGoal) {
                // Set to grey color if the goal is current but not selected
                chip.setChipBackgroundColorResource(R.color.grey);
                chip.setTextColor(getResources().getColor(android.R.color.white)); // Text color for current goals
            } else {
                // Default color for unselected goals
                chip.setChipBackgroundColorResource(R.color.rose_pink);
                chip.setTextColor(getResources().getColor(android.R.color.black)); // Default text color
            }
        }
    }

}
