package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEditContent extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private EditText editTitle, editDescription;
    private TextView uploadDateView;
    private Button saveButton;
    private RecyclerView imagesRecyclerView;
    private List<String> imageUrls;
    private ImagesAdapter imagesAdapter;
    private ImageButton addImage, replaceImage;
    private ImageView back, delete;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String contentId; // Assuming the ID of the content is passed in the intent
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    public static final String ARG_CONTENT_ID = "contentId";
    private static final String TAG = "AdminEditContent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_content);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminEditContent.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        delete = findViewById(R.id.delete);
        editTitle = findViewById(R.id.edit_content_title);
        editDescription = findViewById(R.id.edit_content_description);
        uploadDateView = findViewById(R.id.upload_date_value);
        saveButton = findViewById(R.id.save_button);
        imagesRecyclerView = findViewById(R.id.images_recycler_view);
        addImage = findViewById(R.id.add_image);
        replaceImage = findViewById(R.id.replace_image);

        // Initialize RecyclerView for displaying images
        imageUrls = new ArrayList<>();
        imagesAdapter = new ImagesAdapter(this, imageUrls);  // You'll need to create this adapter class
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imagesAdapter);

        // Get content ID from Intent
        contentId = getIntent().getStringExtra("contentId");

        // Firebase reference to "SkincareTips"
        databaseReference = FirebaseDatabase.getInstance().getReference("SkincareTips").child(contentId);

        // Load existing content details
        loadContentDetails();

        // Disable editing for the date field
        uploadDateView.setEnabled(false);

        // Save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdatedContent();
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        replaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContent();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();  // Get the image URI
            uploadImageToFirebase(imageUri);  // Upload the image to Firebase Storage
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Define storage path (e.g., "images/filename.jpg")
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("Skincare_tips_image")
                    .child(System.currentTimeMillis() + ".jpg");

            // Upload the image
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the uploaded file
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    saveImageUrlToDatabase(downloadUrl);  // Save URL to Firebase Database
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminEditContent.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageUrlToDatabase(String downloadUrl) {
        if (contentId != null) {
            // Reference to the content item in Firebase
            DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("SkincareTips").child(contentId);

            // Add the new image URL to the images list
            contentRef.child("images").push().setValue(downloadUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AdminEditContent.this, "Image added successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminEditContent.this, "Failed to add image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadContentDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String uploadDate = dataSnapshot.child("uploadDateTime").getValue(String.class);

                    // Set text fields
                    editTitle.setText(title);
                    editDescription.setText(description);
                    uploadDateView.setText(uploadDate);

                    // Load multiple images
                    DataSnapshot imagesSnapshot = dataSnapshot.child("images");
                    imageUrls.clear(); // Clear any previous data

                    // Loop through images
                    for (DataSnapshot imageSnapshot : imagesSnapshot.getChildren()) {
                        String imageUrl = imageSnapshot.getValue(String.class);
                        imageUrls.add(imageUrl);  // Add each image URL to the list
                    }

                    // Notify adapter that data has changed
                    imagesAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminEditContent.this, "Content not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data", databaseError.toException());
            }
        });
    }

    private void saveUpdatedContent() {
        String newTitle = editTitle.getText().toString().trim();
        String newDescription = editDescription.getText().toString().trim();

        // Check if title or description is empty
        if (TextUtils.isEmpty(newTitle)) {
            editTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(newDescription)) {
            editDescription.setError("Description is required");
            return;
        }

        // Get the current date and time for when the content was edited
        String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a map to update the fields
        Map<String, Object> updatedContent = new HashMap<>();
        updatedContent.put("title", newTitle);
        updatedContent.put("description", newDescription);
        updatedContent.put("lastEditedDate", currentDateAndTime); // Store the edited date

        // Assuming you're not changing the image in this section, image update logic can be separate

        // Update the content in Firebase
        databaseReference.updateChildren(updatedContent)
                .addOnSuccessListener(aVoid -> {
                    // Success
                    Toast.makeText(AdminEditContent.this, "Content updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity or redirect as needed
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Toast.makeText(AdminEditContent.this, "Failed to update content", Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Failed to update content", e);
                });
    }

    private void deleteContent() {
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Success
                    Toast.makeText(AdminEditContent.this, "Content deleted successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity or redirect as needed
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Toast.makeText(AdminEditContent.this, "Failed to delete content", Toast.LENGTH_SHORT).show();
                    Log.e("Firebase", "Failed to delete content", e);
                });
    }
}
