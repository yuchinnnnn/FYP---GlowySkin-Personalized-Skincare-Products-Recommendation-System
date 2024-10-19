package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEditContent extends AppCompatActivity implements ImagesAdapter.OnImageDeleteListener {
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
    private ImageView back, delete, coverImage;
    private CardView uploadCoverButton;
    private Boolean isCoverImage = false;
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
                Intent intent = new Intent(AdminEditContent.this, AdminManageContent.class);
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
        coverImage = findViewById(R.id.cover_image);
        addImage = findViewById(R.id.add_image);
        replaceImage = findViewById(R.id.replace_image);
        uploadCoverButton = findViewById(R.id.reupload_button);

        uploadCoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePickerForCover();
            }
        });

        // Initialize RecyclerView for displaying images
        imageUrls = new ArrayList<>();
        imagesAdapter = new ImagesAdapter(this, imageUrls, this);  // You'll need to create this adapter class
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
                isCoverImage = false;  // Reset to false for regular images
                openImagePicker();  // Open image picker for regular images
            }
        });

        replaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCoverImage = true;  // Set to true for cover image
                openImagePickerForCover();  // Open image picker for cover image
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContent();
            }
        });
    }

    private void openImagePickerForCover() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Cover Image"), PICK_IMAGE_REQUEST);
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

            // Check if we are uploading the cover image or other images
            if (isCoverImage) {
                uploadCoverImageToFirebase(imageUri);  // Upload the cover image to Firebase Storage
            } else {
                uploadImageToFirebase(imageUri);  // Upload other images
            }
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

    private void uploadCoverImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Define storage path for cover image (e.g., "Skincare_tips_image/userId/contentId_cover.jpg")
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("Skincare_tips_image")
                    .child(userId + "/" + contentId + "_cover.jpg");

            // Upload the cover image
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the uploaded file
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    saveCoverImageUrlToDatabase(downloadUrl);  // Save cover image URL to Firebase Database
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminEditContent.this, "Cover image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCoverImageUrlToDatabase(String downloadUrl) {
        if (contentId != null) {
            // Reference to the content item in Firebase
            DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("SkincareTips").child(contentId);

            // Update the cover image URL in Firebase
            contentRef.child("coverImage").setValue(downloadUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AdminEditContent.this, "Cover image updated successfully", Toast.LENGTH_SHORT).show();
                            // Load the new cover image in the UI
                            Glide.with(AdminEditContent.this).load(downloadUrl).into(coverImage);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminEditContent.this, "Failed to update cover image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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

                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat targetFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
                    Date date = null;
                    try {
                        date = originalFormat.parse(uploadDate);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
                        uploadDate = dateFormat.format(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    uploadDate = targetFormat.format(date);

                    // Assuming title contains spaces or special characters
                    String coverImageFilename = (title + "_cover.jpg").replace(" ", "_"); // Replace spaces with underscores

                    String storageUrl = "https://firebasestorage.googleapis.com/v0/b/personalized-skincare-products.appspot.com/o/Skincare_tips_image%2F"
                            + Uri.encode(userId) + "%2F" + Uri.encode(coverImageFilename) + "?alt=media";


                    Log.d("storageUrl", storageUrl);

                    Glide.with(AdminEditContent.this)
                            .load(storageUrl)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("Glide Error", "Failed to load image: " + e.getMessage());
                                    return false; // Important to return false so the error placeholder can be set
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(coverImage);


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
    @Override
    public void onImageDelete(String imageUrl) {
        // Call the function to delete the image
        deleteImageFromFirebase(imageUrl);
    }

    private void deleteImageFromFirebase(String imageUrl) {
        // Get the reference to the image in Firebase Storage
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        // Delete the image from Firebase Storage
        imageRef.delete().addOnSuccessListener(aVoid -> {
            // Delete the image URL from Firebase Database
            removeImageUrlFromDatabase(imageUrl);
        }).addOnFailureListener(e -> {
            Toast.makeText(AdminEditContent.this, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Firebase", "Failed to delete image", e);
        });
    }

    private void removeImageUrlFromDatabase(String imageUrl) {
        DatabaseReference contentRef = FirebaseDatabase.getInstance().getReference("SkincareTips").child(contentId);

        // Query to find the image URL and remove it
        contentRef.child("images").orderByValue().equalTo(imageUrl).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue(); // Remove the URL from the database
                }
                Toast.makeText(AdminEditContent.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();

                // Remove the image from local list and notify the adapter
                imageUrls.remove(imageUrl);
                imagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Failed to delete image URL", databaseError.toException());
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
