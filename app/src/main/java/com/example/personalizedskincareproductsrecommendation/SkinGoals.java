package com.example.personalizedskincareproductsrecommendation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            // You can choose to finish the activity or handle it accordingly
            finish();
            return;
        }

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        // Change reference to the new SkinGoals table
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinGoals").child(userId);

        others.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show the EditText when "Others" is checked
                othersEditTextContainer.setVisibility(View.VISIBLE);
            } else {
                // Hide the EditText when "Others" is unchecked
                othersEditTextContainer.setVisibility(View.GONE);
            }
        });

        saveButton.setOnClickListener(v -> saveSkinGoals());
        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });
    }

    private void saveSkinGoals() {
        // Retrieve current skin goals from the database
        databaseReference.child("skinGoals").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the current skin goals
                List<String> currentSkinGoals = (List<String>) task.getResult().getValue();

                // Create a string to display the current skin goals
                StringBuilder currentGoalsText = new StringBuilder("Your current skin goals are:\n");
                if (currentSkinGoals != null && !currentSkinGoals.isEmpty()) {
                    for (String goal : currentSkinGoals) {
                        currentGoalsText.append("- ").append(goal).append("\n");
                    }
                } else {
                    currentGoalsText.append("No goals set.");
                }

                // Create a list to store the selected goals
                List<String> selectedGoals = new ArrayList<>();

                // Iterate through the children of the ChipGroup
                for (int i = 0; i < skinGoalsChipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) skinGoalsChipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        if (chip.getId() == R.id.others) {
                            // Add the text from the EditText if "Others" is selected
                            String othersGoal = othersEditText.getText().toString();
                            if (!TextUtils.isEmpty(othersGoal)) {
                                selectedGoals.add(othersGoal);
                            }
                        } else {
                            selectedGoals.add(chip.getText().toString());
                        }
                    }
                }

                // Show a confirmation dialog with current skin goals before saving the new ones
                new SweetAlertDialog(SkinGoals.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Change")
                        .setContentText(currentGoalsText.toString() + "\nDo you want to update your skin goals?")
                        .setConfirmText("Yes, save it!")
                        .setCancelText("No, keep current goals")
                        .setConfirmClickListener(sDialog -> {
                            // If user confirms, proceed to save the new skin goals
                            sDialog.dismissWithAnimation();

                            // Create a map to update the skin goals in the database
                            Map<String, Object> skinGoalsUpdate = new HashMap<>();
                            skinGoalsUpdate.put("skinGoals", selectedGoals);

                            // Update the user's skin goals in the database
                            databaseReference.updateChildren(skinGoalsUpdate)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            // If the update is successful, show a success message
                                            new SweetAlertDialog(SkinGoals.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Saved!")
                                                    .setContentText("Your skin goals have been updated successfully.")
                                                    .show();
                                        } else {
                                            // If the update fails, show an error message
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
                // Handle the case where retrieving the current skin goals fails
                Toast.makeText(SkinGoals.this, "Failed to retrieve current skin goals. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
