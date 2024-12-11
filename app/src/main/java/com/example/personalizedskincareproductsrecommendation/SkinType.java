package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
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
import java.util.Arrays;
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

        userId = getIntent().getStringExtra(ARG_USER_ID);
        if (userId == null) {
            // Handle the case where userId is not passed or retrieved
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Set onClickListeners for each Chip
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
            intent.putExtra("ARG_USER_ID", userId);
            startActivity(intent);
        });

        saveButton.setOnClickListener(v -> confirmationDialog());
        back.setOnClickListener(v -> {
            finish();
        });

        underlineText(skinAnalysis, "Do the skin analysis to find out");

        // Highlight current skin types
        highlightCurrentSkinTypes();

        // Add chip checked change listener
        skinTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> highlightCurrentSkinTypes());
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

    private void highlightCurrentSkinTypes() {
        DatabaseReference skinTypesReference = FirebaseDatabase.getInstance().getReference("SkinTypes").child(userId);

        skinTypesReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object result = task.getResult().getValue();

                if (result instanceof String) {
                    // If the result is a String, cast it directly
                    String currentSkinTypesString = (String) result;
                    highlightChips(currentSkinTypesString);
                } else if (result instanceof Map) {
                    // If it's a Map, handle accordingly (if the structure is more complex)
                    Map<String, Object> skinTypesMap = (Map<String, Object>) result;
                    String currentSkinTypesString = (String) skinTypesMap.get("skinType");  // Extract the skinType value
                    highlightChips(currentSkinTypesString);
                }
            } else {
                Toast.makeText(SkinType.this, "Failed to retrieve current skin types.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void highlightChips(String currentSkinTypesString) {
        List<String> currentSkinTypes = currentSkinTypesString != null ?
                Arrays.asList(currentSkinTypesString.split(",\\s*")) : new ArrayList<>();

        for (int i = 0; i < skinTypeChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) skinTypeChipGroup.getChildAt(i);
            String chipText = chip.getText().toString();
            boolean isCurrentType = currentSkinTypes.contains(chipText);
            boolean isSelectedType = chip.isChecked();

            if (isSelectedType) {
                chip.setChipBackgroundColorResource(R.color.black);  // Highlight for selected
                chip.setTextColor(getResources().getColor(android.R.color.white));  // Text color for selected
            } else if (isCurrentType) {
                chip.setChipBackgroundColorResource(R.color.grey);  // Grey for current
                chip.setTextColor(getResources().getColor(android.R.color.white));  // Text color for current
            } else {
                chip.setChipBackgroundColorResource(R.color.rose_pink);  // Default color
                chip.setTextColor(getResources().getColor(android.R.color.black));  // Default text color
            }
        }
    }



    private void confirmationDialog() {
        new SweetAlertDialog(SkinType.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you Sure?")
                .setContentText("Do you want to save your skin types?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    saveSkinTypes();
                    finish();
                })
                .setCancelClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    finish();
                })
                .show();
    }

    private void saveSkinTypes() {
        List<String> selectedSkinTypes = getSelectedSkinTypes();
        // Join the selected skin types into a single comma-separated string
        String skinTypesString = TextUtils.join(", ", selectedSkinTypes);

        // Create a new reference for the skin types in a separate table
        DatabaseReference skinTypesReference = FirebaseDatabase.getInstance().getReference("SkinTypes");

        // Use the userId to create a unique entry for this user's skin types
        skinTypesReference.child(userId).child("skinType").setValue(skinTypesString) // Changed "skin type:" to "skinType"
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SkinType.this, "Skin types saved successfully", Toast.LENGTH_SHORT).show();
                        highlightCurrentSkinTypes(); // Refresh the highlighting
                    } else {
                        Toast.makeText(SkinType.this, "Failed to save skin types", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
