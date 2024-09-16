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
            if (selectedSkinType != null) {
                if (existingSkinType != null && !selectedSkinType.equals(existingSkinType)) {
                    new SweetAlertDialog(SkinTypeQuiz1.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Change")
                            .setContentText("Are you sure you want to change your answer?")
                            .setConfirmText("Yes, change it!")
                            .setCancelText("No, keep it")
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                updateSkinTypeInDatabase();
                            })
                            .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                } else {
                    updateSkinTypeInDatabase();
                }
            } else {
                Toast.makeText(SkinTypeQuiz1.this, "Please select an answer", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });
    }

    private void loadExistingSkinType() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                existingSkinType = dataSnapshot.getValue(String.class);
                if (existingSkinType != null) {
                    // Highlight the previously selected skin type
                    switch (existingSkinType) {
                        case "Dry":
                            selectSkinType("It feels dry and flakey sometimes irritated", dry);
                            break;
                        case "Normal":
                            selectSkinType("It feels same as in the morning, great!", normal);
                            break;
                        case "Combination":
                            selectSkinType("Dry cheeks, but nose and forehead are shiny", combination);
                            break;
                        case "Oil":
                            selectSkinType("My skin becomes shiny throughout the day", oil);
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz1.this, "Failed to load existing answer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectSkinType(String skinType, LinearLayout selectedLayout) {
        dry.setBackgroundColor(getResources().getColor(R.color.light_grey));
        normal.setBackgroundColor(getResources().getColor(R.color.light_grey));
        combination.setBackgroundColor(getResources().getColor(R.color.light_grey));
        oil.setBackgroundColor(getResources().getColor(R.color.light_grey));

        selectedLayout.setBackgroundColor(getResources().getColor(R.color.pink));
        selectedSkinType = skinType;
    }

    private void updateSkinTypeInDatabase() {
        databaseReference.setValue(selectedSkinType)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SkinTypeQuiz1.this, SkinTypeQuiz2.class);
                        intent.putExtra(ARG_USER_ID, userId);
                        intent.putExtra(ARG_SELECTED_SKIN_TYPE, selectedSkinType);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SkinTypeQuiz1.this, "Failed to save answer", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
