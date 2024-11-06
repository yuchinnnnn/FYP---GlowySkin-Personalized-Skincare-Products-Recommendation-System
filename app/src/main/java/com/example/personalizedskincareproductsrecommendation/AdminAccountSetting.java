package com.example.personalizedskincareproductsrecommendation;

import static android.content.ContentValues.TAG;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminAccountSetting extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private DatabaseReference databaseReference;
    private CircleImageView profilePic;
    private ImageView uploadButton, editContactButton, back;
    private TextView usernameText, emailText, contactText;
    private Button saveButton;
    StorageReference storageReference;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account_setting);
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userId);


        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminAccountSetting.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        // Initialize views
        profilePic = findViewById(R.id.profile_pic);
        uploadButton = findViewById(R.id.upload_button);
        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        contactText = findViewById(R.id.contact_text);
        editContactButton = findViewById(R.id.edit_contact_button);
        saveButton = findViewById(R.id.save_button);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId != null) {
            Log.d(TAG, "User ID: " + userId);
            databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String contact = snapshot.child("contact").getValue(String.class);

                        if (username != null) {
                            usernameText.setText(username);
                            Log.d(TAG, "Username: " + username);
                        } else {
                            Log.d(TAG, "Username not found");
                        }

                        if (email != null) {
                            emailText.setText(email);
                            Log.d(TAG, "Email: " + email);
                        } else {
                            Log.d(TAG, "Email not found");
                        }

                        if (contact != null) {
                            contactText.setText(contact);
                            Log.d(TAG, "Contact: " + contact);
                        } else {
                            Log.d(TAG, "Contact not found");
                        }
                    } else {
                        Log.d(TAG, "Snapshot does not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });

//          storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userId);
            loadProfilePhoto(userId);
        } else {
            Log.d(TAG, "User ID is null");
        }

        // Handle profile picture upload
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Handle editing the contact information
        editContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditContactDialog();
            }
        });

        // Handle saving profile information
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newContact = contactText.getText().toString().trim();
                updateUserProfile(newContact);
            }
        });
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
                        Glide.with(AdminAccountSetting.this).load(profilePhotoUrl).into(profilePic);
                        Log.d(TAG, "Profile photo loaded successfully.");
                    } else {
                        Log.d(TAG, "Profile photo URL is null.");
                        // Load a default image if profile photo URL is null
                        profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile photo URL: " + error.getMessage());
                // Load a default image in case of error
                profilePic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
            }
        });
    }

    // Open gallery to select a profile picture
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
        }
    }

    private void showEditContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Contact Number");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newContact = input.getText().toString();
            if (!TextUtils.isEmpty(newContact)) {
                contactText.setText(newContact);
                Toast.makeText(AdminAccountSetting.this, "Contact updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AdminAccountSetting.this, "Contact cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserProfile(String newContact) {
        if (TextUtils.isEmpty(newContact)) {
            Toast.makeText(AdminAccountSetting.this, "Invalid input data", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("contact", newContact);

        databaseReference.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminAccountSetting.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        if (imageUri != null) {
                            uploadProfilePhoto();
                        }
                    } else {
                        Toast.makeText(AdminAccountSetting.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AdminAccountSetting.this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminAccountSetting.this, "Failed to upload profile photo", Toast.LENGTH_SHORT).show();
        });
    }

    // Function to update user profile in the database (e.g., Firebase)
//    private void updateUserProfile(String username, String email, String contact) {
//        // Example Firebase database update
//
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Admin");
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        Map<String, Object> userUpdates = new HashMap<>();
//        userUpdates.put("username", username);
//        userUpdates.put("email", email);
//        userUpdates.put("contact", contact);
//
//        databaseReference.child(userId).updateChildren(userUpdates)
//            .addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Toast.makeText(AdminAccountSetting.this, "Profile Updated", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(AdminAccountSetting.this, "Update Failed", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//    }
}