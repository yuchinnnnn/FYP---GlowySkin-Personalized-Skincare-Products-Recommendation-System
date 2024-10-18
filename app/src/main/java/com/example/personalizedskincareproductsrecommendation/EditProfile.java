package com.example.personalizedskincareproductsrecommendation;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.app.AlertDialog;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditProfile extends AppCompatActivity {
    private static final String TAG = "EditProfile";

    private ImageView profile_pic, back;
    private TextView username_text, email_text, age_text, gender_text;
    private Button save;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private String originalUsername;
    private int originalAge;
    private String originalGender;


    StorageReference storageReference;
    Uri imageUri;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        profile_pic.setImageURI(imageUri);  // Preview the selected image
                    }
                }
            }
    );

    private static final String ARG_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        back = findViewById(R.id.back);
        save = findViewById(R.id.save_button);
        profile_pic = findViewById(R.id.profile_pic);
        username_text = findViewById(R.id.username_text);
        email_text = findViewById(R.id.email_text);
        age_text = findViewById(R.id.age_text);
        gender_text = findViewById(R.id.gender_text);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userId);

        profile_pic.setOnClickListener(v -> showUploadImageDialog());

        loadProfilePhoto(userId);

        back.setOnClickListener(v -> finish());  // Navigate back to the previous activity

        save.setOnClickListener(v -> new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Do you want to save the changes?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    String newUsername = username_text.getText().toString();
                    String ageStr = age_text.getText().toString();
                    String newGender = gender_text.getText().toString();

                    // Check if fields are empty
                    if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(newGender)) {
                        Toast.makeText(EditProfile.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newAge;
                    try {
                        newAge = Integer.parseInt(ageStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(EditProfile.this, "Invalid age format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate if any changes were made
                    if (newUsername.equals(originalUsername) && newAge == originalAge && newGender.equals(originalGender)) {
                        Toast.makeText(EditProfile.this, "No changes detected", Toast.LENGTH_SHORT).show();
                    } else {
                        saveProfileChanges(newUsername, newAge, newGender);
                    }
                })
                .show());


        username_text.setOnClickListener(v -> showEditUsernameDialog());
        age_text.setOnClickListener(v -> showAgePickerDialog());
        gender_text.setOnClickListener(v -> showGenderSelectionDialog());

        loadUserProfileData();
    }

    private void loadProfilePhoto(String userId) {
        // Construct the path to the user's profile photo URL
        DatabaseReference profileImageRef = databaseReference.child("profilePhotoUrl");

        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the profilePhotoUrl exists
                if (snapshot.exists()) {
                    String profilePhotoUrl = snapshot.getValue(String.class);
                    if (profilePhotoUrl != null) {
                        // Load the image using the URL with Glide
                        Glide.with(EditProfile.this).load(profilePhotoUrl).into(profile_pic);
                        Log.d(TAG, "Profile photo loaded successfully.");
                    } else {
                        Log.d(TAG, "Profile photo URL is null.");
                        // Load a default image if profile photo URL is null
                        profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile photo URL: " + error.getMessage());
                // Load a default image in case of error
                profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
            }
        });
    }


    private void loadUserProfileData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    Long ageLong = snapshot.child("age").getValue(Long.class);
                    String gender = snapshot.child("gender").getValue(String.class);

                    // Set the original values for comparison
                    originalUsername = username != null ? username : "";
                    originalAge = ageLong != null ? ageLong.intValue() : 0;
                    originalGender = gender != null ? gender : "";

                    // Populate UI fields
                    username_text.setText(originalUsername);
                    email_text.setText(email != null ? email : "N/A");
                    age_text.setText(String.valueOf(originalAge));
                    gender_text.setText(originalGender);
                } else {
                    Log.d(TAG, "Snapshot does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }


    private void showUploadImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            activityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Username");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUsername = input.getText().toString();
            if (!TextUtils.isEmpty(newUsername)) {
                username_text.setText(newUsername);
                Toast.makeText(EditProfile.this, "Username updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditProfile.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showAgePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Age");

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(12);
        numberPicker.setMaxValue(100);

        builder.setView(numberPicker);

        builder.setPositiveButton("Save", (dialog, which) -> {
            int selectedAge = numberPicker.getValue();
            age_text.setText(String.valueOf(selectedAge));
            Toast.makeText(EditProfile.this, "Age updated to " + selectedAge, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showGenderSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender");

        final String[] genderOptions = {"Male", "Female", "Not to Say"};
        int checkedItem = -1;

        builder.setSingleChoiceItems(genderOptions, checkedItem, (dialog, which) -> {
            String selectedGender = genderOptions[which];
            gender_text.setText(selectedGender);
            Toast.makeText(EditProfile.this, "Gender updated to " + selectedGender, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveProfileChanges(String newUsername, int newAge, String newGender) {
        if (TextUtils.isEmpty(newUsername) || newAge <= 0 || TextUtils.isEmpty(newGender)) {
            Toast.makeText(EditProfile.this, "Invalid input data", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("age", newAge);
        updates.put("gender", newGender);

        databaseReference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        if (imageUri != null) {
                            uploadProfilePhoto();
                        }
                    } else {
                        Toast.makeText(EditProfile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadProfilePhoto() {
        if (imageUri == null) return;

        StorageReference photoRef = storageReference.child("profile_photo.jpg");
        UploadTask uploadTask = photoRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                databaseReference.child("profilePhotoUrl").setValue(uri.toString());
                Toast.makeText(EditProfile.this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(EditProfile.this, "Failed to upload profile photo", Toast.LENGTH_SHORT).show();
        });
    }
}
