package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SkinType extends AppCompatActivity {

    private ChipGroup skinTypeChipGroup;
    private Chip normal, normal_sensitive, oily, oil_sensitive, combination, dry, sensitive, sensitive_dry, combination_sensitive;
    private Button saveButton;
    private ImageView back;

    private TextView skinAnalysis;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private static final String ARG_USER_ID = "userId";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type);

        saveButton = findViewById(R.id.save_button);
        back = findViewById(R.id.back_button);
        skinAnalysis = findViewById(R.id.skin_analysis_button);

        skinTypeChipGroup = findViewById(R.id.skin_types_chip_group);
        normal = findViewById(R.id.normal);
        normal_sensitive = findViewById(R.id.normal_sensitive);
        oily = findViewById(R.id.oily);
        oil_sensitive = findViewById(R.id.oil_sensitive);
        combination = findViewById(R.id.combination);
        combination_sensitive = findViewById(R.id.combination_sensitive);
        dry = findViewById(R.id.dry);
        sensitive = findViewById(R.id.sensitive);
        sensitive_dry = findViewById(R.id.dry_sensitive);

        // Retrieve user ID from intent
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            // If userId is null, fall back to FirebaseAuth's current user ID
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            userId = mAuth.getCurrentUser().getUid();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Set onClickListeners for each Chip to show Toast messages
        setChipClickListener(normal, "Normal");
        setChipClickListener(normal_sensitive, "Normal + Sensitive");
        setChipClickListener(oily, "Oily");
        setChipClickListener(oil_sensitive, "Oily + Sensitive");
        setChipClickListener(combination, "Combination");
        setChipClickListener(combination_sensitive, "Combination + Sensitive");
        setChipClickListener(dry, "Dry");
        setChipClickListener(sensitive, "Sensitive");
        setChipClickListener(sensitive_dry, "Dry + Sensitive");

        skinAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(SkinType.this, SkinAnalysis.class);
            startActivity(intent);
        });

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        saveButton.setOnClickListener(v -> saveSkinTypes());
        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });

        // Set underlined text for skin analysis TextView
        underlineText(skinAnalysis, "Do the skin analysis to find out");
    }

    private void setChipClickListener(Chip chip, String chipText) {
        chip.setOnClickListener(v -> {
            if (chip.isChecked()) {
                Toast.makeText(SkinType.this, chipText + " selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SkinType.this, chipText + " deselected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void underlineText(TextView textView, String text) {
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
    }

    private List<String> getSelectedSkinTypes() {
        List<String> selectedSkinTypes = new ArrayList<>();
        for (int i = 0; i < skinTypeChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) skinTypeChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedSkinTypes.add(chip.getText().toString());
            }
        }
        return selectedSkinTypes;
    }

    private void saveSkinTypes() {
        // Retrieve current skin types from the database
        databaseReference.child(userId).child("skinTypes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the current skin types
                List<String> currentSkinTypes = (List<String>) task.getResult().getValue();

                // Create a string to display the current skin types
                StringBuilder currentTypesText = new StringBuilder("Your current skin types are:\n");
                if (currentSkinTypes != null && !currentSkinTypes.isEmpty()) {
                    for (String type : currentSkinTypes) {
                        currentTypesText.append("- ").append(type).append("\n");
                    }
                } else {
                    currentTypesText.append("No types set.");
                }

                // Show a confirmation dialog with current skin types before saving the new ones
                new SweetAlertDialog(SkinType.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Confirm Change")
                        .setContentText(currentTypesText.toString() + "\nDo you want to update your skin types?")
                        .setConfirmText("Yes, save it!")
                        .setCancelText("No, keep current types")
                        .setConfirmClickListener(sDialog -> {
                            // If user confirms, proceed to save the new skin types
                            sDialog.dismissWithAnimation();

                            List<String> selectedSkinTypes = getSelectedSkinTypes();

                            // Create a map to update the skin types in the database
                            Map<String, Object> skinTypeMap = new HashMap<>();
                            for (int i = 0; i < selectedSkinTypes.size(); i++) {
                                skinTypeMap.put(String.valueOf(i), selectedSkinTypes.get(i));
                            }

                            // Update the user's skin types in the database
                            databaseReference.child(userId).child("skinTypes").setValue(skinTypeMap)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            // If the update is successful, show a success message
                                            new SweetAlertDialog(SkinType.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Saved!")
                                                    .setContentText("Your skin types have been updated successfully.")
                                                    .show();
                                        } else {
                                            // If the update fails, show an error message
                                            new SweetAlertDialog(SkinType.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Oops...")
                                                    .setContentText("Failed to save skin types. Please try again.")
                                                    .show();
                                        }
                                    });
                        })
                        .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
            } else {
                // Handle the case where retrieving the current skin types fails
                Toast.makeText(SkinType.this, "Failed to retrieve current skin types. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

// package com.example.personalizedskincareproductsrecommendation;
//
//        import androidx.appcompat.app.AppCompatActivity;
//        import android.content.Intent;
//        import android.os.Bundle;
//        import android.text.SpannableString;
//        import android.text.style.UnderlineSpan;
//        import android.widget.Button;
//        import android.widget.ImageView;
//        import android.widget.TextView;
//        import android.widget.Toast;
//
//        import com.google.android.material.chip.Chip;
//        import com.google.android.material.chip.ChipGroup;
//        import com.google.firebase.auth.FirebaseAuth;
//        import com.google.firebase.database.DatabaseReference;
//        import com.google.firebase.database.FirebaseDatabase;
//
//        import java.util.ArrayList;
//        import java.util.HashMap;
//        import java.util.List;
//        import java.util.Map;
//
//public class SkinType extends AppCompatActivity {
//
//    private ChipGroup skinTypeChipGroup;
//    private Chip normal, normal_sensitive, oily, oil_sensitive, combination, dry, sensitive, sensitive_dry, combination_sensitive;
//    private Button saveButton;
//    private ImageView back;
//
//    private TextView skinAnalysis;
//    private DatabaseReference databaseReference;
//    private FirebaseAuth mAuth;
//    private String userId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_skin_type);
//
//        saveButton = findViewById(R.id.save_button);
//        back = findViewById(R.id.back_button);
//        skinAnalysis = findViewById(R.id.skin_analysis_button);
//
//        skinTypeChipGroup = findViewById(R.id.skin_types_chip_group);
//        normal = findViewById(R.id.normal);
//        normal_sensitive = findViewById(R.id.normal_sensitive);
//        oily = findViewById(R.id.oily);
//        oil_sensitive = findViewById(R.id.oil_sensitive);
//        combination = findViewById(R.id.combination);
//        combination_sensitive = findViewById(R.id.combination_sensitive);
//        dry = findViewById(R.id.dry);
//        sensitive = findViewById(R.id.sensitive);
//        sensitive_dry = findViewById(R.id.dry_sensitive);
//
//        // Retrieve user ID from intent
//        userId = getIntent().getStringExtra("userId");
//
//        if (userId == null) {
//            // If userId is null, fall back to FirebaseAuth's current user ID
//            FirebaseAuth mAuth = FirebaseAuth.getInstance();
//            userId = mAuth.getCurrentUser().getUid();
//        }
//
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
//
//        // Set onClickListeners for each Chip to show Toast messages
//        setChipClickListener(normal, "Normal");
//        setChipClickListener(normal_sensitive, "Normal + Sensitive");
//        setChipClickListener(oily, "Oily");
//        setChipClickListener(oil_sensitive, "Oily + Sensitive");
//        setChipClickListener(combination, "Combination");
//        setChipClickListener(combination_sensitive, "Combination + Sensitive");
//        setChipClickListener(dry, "Dry");
//        setChipClickListener(sensitive, "Sensitive");
//        setChipClickListener(sensitive_dry, "Dry + Sensitive");
//
//        skinAnalysis.setOnClickListener(v -> {
//            Intent intent = new Intent(SkinType.this, SkinAnalysis.class);
//            startActivity(intent);
//        });
//
//        mAuth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
//        userId = mAuth.getCurrentUser().getUid();
//
//        saveButton.setOnClickListener(v -> saveSkinTypes());
//        back.setOnClickListener(v -> {
//            HomeFragment homeFragment = HomeFragment.newInstance(userId);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, homeFragment)
//                    .commit();
//        });
//
//        // Set underlined text for skin analysis TextView
//        underlineText(skinAnalysis, "Do the skin analysis to find out");
//    }
//
//    private void setChipClickListener(Chip chip, String chipText) {
//        chip.setOnClickListener(v -> {
//            if (chip.isChecked()) {
//                Toast.makeText(SkinType.this, chipText + " selected", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(SkinType.this, chipText + " deselected", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void underlineText(TextView textView, String text) {
//        SpannableString content = new SpannableString(text);
//        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//        textView.setText(content);
//    }
//
//    private List<String> getSelectedSkinTypes() {
//        List<String> selectedSkinTypes = new ArrayList<>();
//        for (int i = 0; i < skinTypeChipGroup.getChildCount(); i++) {
//            Chip chip = (Chip) skinTypeChipGroup.getChildAt(i);
//            if (chip.isChecked()) {
//                selectedSkinTypes.add(chip.getText().toString());
//            }
//        }
//        return selectedSkinTypes;
//    }
//
//    private void saveSkinTypes() {
//        List<String> selectedSkinTypes = getSelectedSkinTypes();
//        Map<String, Object> skinTypeMap = new HashMap<>();
//        for (int i = 0; i < selectedSkinTypes.size(); i++) {
//            skinTypeMap.put(String.valueOf(i), selectedSkinTypes.get(i));
//        }
//
//        databaseReference.child(userId).child("skinTypes").setValue(skinTypeMap)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(SkinType.this, "Skin types saved successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(SkinType.this, "Failed to save skin types", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//}