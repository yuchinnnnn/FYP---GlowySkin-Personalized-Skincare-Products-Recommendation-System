package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AdminAddContent extends AppCompatActivity {
    private ImageView back, uploadedImage;
    private EditText title, description;
    private Button uploadImageButton, saveButton, cancelButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";

    StorageReference storageReference;
    Uri imageUri;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadedImage.setImageURI(imageUri);  // Preview the selected image
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_content);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("Skincare_tips_image").child(userId);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminAddContent.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        title = findViewById(R.id.tipTitle);
        description = findViewById(R.id.tipDescription);
        uploadedImage = findViewById(R.id.uploadedImage);

        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> showUploadImageDialog());
        loadUploadedImage(userId);


        saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(v -> {
            String titleText = title.getText().toString().trim();
            String descriptionText = description.getText().toString().trim();
            if (titleText.isEmpty()) {
                title.setError("Title is required");
                title.requestFocus();
                return;
            }
            if (descriptionText.isEmpty()) {
                description.setError("Description is required");
                description.requestFocus();
                return;
            }
            saveContent(titleText, descriptionText);
        });

        cancelButton = findViewById(R.id.btnCancel);
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

    private void loadUploadedImage(String userId) {
        // Construct the path to the user's profile photo URL
        DatabaseReference profileImageRef = databaseReference.child("skincare_tips_image").child(userId);

        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the profilePhotoUrl exists
                if (snapshot.exists()) {
                    String profilePhotoUrl = snapshot.getValue(String.class);
                    if (profilePhotoUrl != null) {
                        // Load the image using the URL with Glide
                        Glide.with(AdminAddContent.this).load(profilePhotoUrl).into(uploadedImage);
                        Log.d(TAG, "Skincare tips photo loaded successfully.");
                    } else {
                        Log.d(TAG, "Skincare tips photo URL is null.");
                        // Load a default image if profile photo URL is null
                        uploadedImage.setImageResource(R.drawable.skin_care); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    uploadedImage.setImageResource(R.drawable.skin_care); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load skincare tips photo URL: " + error.getMessage());
                // Load a default image in case of error
                uploadedImage.setImageResource(R.drawable.skin_care); // Set your default image here
            }
        });
    }

    private void saveContent(String tipsTitle, String tipsDescription) {
        if (TextUtils.isEmpty(tipsTitle) || TextUtils.isEmpty(tipsDescription)) {
            Toast.makeText(AdminAddContent.this, "Invalid input data", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("tipsTitle", tipsTitle);
        updates.put("tipsDescription", tipsDescription);

        databaseReference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminAddContent.this, "Content updated successfully", Toast.LENGTH_SHORT).show();
                        if (imageUri != null) {
                            uploadContentPhoto();
                        }
                    } else {
                        Toast.makeText(AdminAddContent.this, "Failed to update content", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadContentPhoto() {
        if (imageUri == null) return;

        StorageReference photoRef = storageReference.child("skincare_tips_image.jpg");
        UploadTask uploadTask = photoRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                databaseReference.child("Skincare_tips_image").setValue(uri.toString());
                Toast.makeText(AdminAddContent.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminAddContent.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }
}