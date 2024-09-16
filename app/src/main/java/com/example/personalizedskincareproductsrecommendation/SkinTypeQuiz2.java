package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinTypeQuiz2 extends AppCompatActivity {

    private ImageView next, previous, back;
    private CheckBox lavender, citrus, tea_trees, cica, none;
    private DatabaseReference databaseReference;
    private List<String> selectedSensitives;

    private FirebaseAuth mAuth;
    private String userId;
    private List<String> existingSensitives;

    private static final String ARG_USER_ID = "userId";
    private static final String ARG_SELECTED_SKIN_TYPE = "selectedSensitive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_type_quiz2);

        lavender = findViewById(R.id.lavender);
        citrus = findViewById(R.id.citrus);
        tea_trees = findViewById(R.id.tea_trees);
        cica = findViewById(R.id.cica);
        none = findViewById(R.id.none);

        selectedSensitives = new ArrayList<>();
        existingSensitives = new ArrayList<>();

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinQuiz").child(userId).child("skinTypeQuiz2");

        // Load existing selected sensitivities from Firebase
        loadExistingSensitives();

        lavender.setOnClickListener(v -> toggleSelection("lavender", lavender));
        citrus.setOnClickListener(v -> toggleSelection("citrus", citrus));
        tea_trees.setOnClickListener(v -> toggleSelection("tea_trees", tea_trees));
        cica.setOnClickListener(v -> toggleSelection("cica", cica));
        none.setOnClickListener(v -> toggleSelection("none", none));

        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        back = findViewById(R.id.back_button);

        next.setOnClickListener(v -> {
            if (!selectedSensitives.isEmpty()) {
                if (!existingSensitives.equals(selectedSensitives)) {
                    new SweetAlertDialog(SkinTypeQuiz2.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Confirm Change")
                            .setContentText("Are you sure you want to change your answer?")
                            .setConfirmText("Yes, change it!")
                            .setCancelText("No, keep it")
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                updateSensitiveInDatabase();
                            })
                            .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                } else {
                    updateSensitiveInDatabase();
                }
            } else {
                Toast.makeText(SkinTypeQuiz2.this, "Please select an answer", Toast.LENGTH_SHORT).show();
            }
        });

        previous.setOnClickListener(v -> {
            Intent intent = new Intent(SkinTypeQuiz2.this, SkinTypeQuiz1.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });
    }

    private void loadExistingSensitives() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                existingSensitives.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    existingSensitives.add(snapshot.getValue(String.class));
                }
                highlightExistingSensitives();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SkinTypeQuiz2.this, "Failed to load existing answer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleSelection(String sensitive, CheckBox checkBox) {
        if (selectedSensitives.contains(sensitive)) {
            selectedSensitives.remove(sensitive);
            checkBox.setChecked(false);
        } else {
            selectedSensitives.add(sensitive);
            checkBox.setChecked(true);
        }
    }

    private void updateSensitiveInDatabase() {
        databaseReference.setValue(selectedSensitives)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(SkinTypeQuiz2.this, SkinTypeQuiz3.class);
                        intent.putExtra(ARG_USER_ID, userId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(SkinTypeQuiz2.this, "Failed to save answer", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void highlightExistingSensitives() {
        for (String sensitive : existingSensitives) {
            switch (sensitive) {
                case "lavender":
                    lavender.setChecked(true);
                    selectedSensitives.add("lavender");
                    break;
                case "citrus":
                    citrus.setChecked(true);
                    selectedSensitives.add("citrus");
                    break;
                case "tea_trees":
                    tea_trees.setChecked(true);
                    selectedSensitives.add("tea_trees");
                    break;
                case "cica":
                    cica.setChecked(true);
                    selectedSensitives.add("cica");
                    break;
                case "none":
                    none.setChecked(true);
                    selectedSensitives.add("none");
                    break;
            }
        }
    }
}
