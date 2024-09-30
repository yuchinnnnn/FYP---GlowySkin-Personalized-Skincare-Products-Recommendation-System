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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cn.pedant.SweetAlert.SweetAlertDialog;
import android.widget.ArrayAdapter;
public class AdminAddContent extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private AutoCompleteTextView categoryDropdown; // category dropdown
    private ImageView back, uploadedImage;
    private TextInputEditText title, description;
    private Button uploadImageButton, uploadButton, cancelButton;
    private List<Uri> uploadedImages = new ArrayList<>();

    private DatabaseReference databaseReference, skincareTipsDatabaseReference;


    StorageReference storageReference;
    Uri imageUri;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uploadedImages.clear(); // Create a List<Uri> uploadedImages to store image Uris
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                uploadedImages.add(imageUri); // Add each image Uri to the list
                            }
                        } else if (result.getData().getData() != null) {
                            Uri imageUri = result.getData().getData();
                            uploadedImages.add(imageUri); // If a single image was selected
                        }
                        // Display the first image or show a count of selected images
                        if (!uploadedImages.isEmpty()) {
                            uploadedImage.setImageURI(uploadedImages.get(0)); // Show the first image as a preview
                            Toast.makeText(AdminAddContent.this, uploadedImages.size() + " images selected.", Toast.LENGTH_SHORT).show();
                        }
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

        skincareTipsDatabaseReference = FirebaseDatabase.getInstance().getReference("SkincareTips");

        categoryDropdown = findViewById(R.id.hint_text);
        // Define the list of categories (hardcoded or retrieved from database)
        String[] categories = new String[] {"For All Skin Type", "For Oily Skin", "For Dry Skin", "For Combination Skin", "For Sensitive Skin"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);

        // Set the adapter to the AutoCompleteTextView
        categoryDropdown.setAdapter(adapter);
        String selectedCategory = categoryDropdown.getText().toString();

        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        uploadedImage = findViewById(R.id.uploadedImage);

        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadImageButton.setOnClickListener(v -> showUploadImageDialog());
        loadUploadedImage(userId);


        uploadButton = findViewById(R.id.UploadButton);
        uploadButton.setOnClickListener(v -> {
            String titleText = title.getText().toString().trim();
            String descriptionText = description.getText().toString().trim();
            String category = categoryDropdown.getText().toString().trim(); // Get category

            // Validate input fields
            if (category.isEmpty()) {
                categoryDropdown.setError("Category is required");
                categoryDropdown.requestFocus();
                return;
            }
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

            // Save the content
            saveContent(selectedCategory, titleText, descriptionText);
            showConfirmationDialog();

        });

        cancelButton = findViewById(R.id.CancelButton);
    }

    private void showUploadImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Images");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple selections
            intent.setType("image/*");
            activityResultLauncher.launch(Intent.createChooser(intent, "Select Images"));
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showConfirmationDialog() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(AdminAddContent.this, SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitle("Upload Successful");
        sweetAlertDialog.setContentText("Your content has been uploaded successfully.");
        sweetAlertDialog.setConfirmText("OK");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
                Intent intent = new Intent(AdminAddContent.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });
        sweetAlertDialog.show();
        title.setText("");
        description.setText("");
        uploadedImage.setImageResource(R.drawable.skin_care); // Reset image preview
    }

    private void loadUploadedImage(String userId) {
        // Construct the path to the user's profile photo URL
        DatabaseReference profileImageRef = skincareTipsDatabaseReference.child("skincare_tips_image").child(userId);

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
                        uploadedImage.setImageResource(R.drawable.baseline_image_24); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    uploadedImage.setImageResource(R.drawable.baseline_image_24); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load skincare tips photo URL: " + error.getMessage());
                // Load a default image in case of error
                uploadedImage.setImageResource(R.drawable.baseline_image_24); // Set your default image here
            }
        });
    }

    private void saveContent(String tipsCategory, String tipsTitle, String tipsDescription) {
        if (TextUtils.isEmpty(tipsTitle) || TextUtils.isEmpty(tipsDescription)) {
            Toast.makeText(AdminAddContent.this, "Invalid input data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter); // e.g., 2023-09-28 14:30:15

        // Generate a unique key for each skincare tip
        String tipId = skincareTipsDatabaseReference.push().getKey();

        if (tipId != null) {
            // Prepare the data to save, including the userId and current date/time
            Map<String, Object> tipsData = new HashMap<>();
            tipsData.put("id", tipId);
            tipsData.put("category", tipsCategory); // Save the category
            tipsData.put("title", tipsTitle);
            tipsData.put("description", tipsDescription);
            tipsData.put("userId", userId);  // Add userId of the admin who uploads the tips
            tipsData.put("uploadDateTime", formattedDateTime); // Store current date/time

            // Save the tips data under the unique key in the "SkincareTips" node
            skincareTipsDatabaseReference.child(tipId).updateChildren(tipsData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminAddContent.this, "Content saved successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminAddContent.this, AdminDashboard.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            uploadContentPhotos(tipId); // Upload the selected images
                        } else {
                            Toast.makeText(AdminAddContent.this, "Failed to save content", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadContentPhotos(String tipId) {
        if (uploadedImages.isEmpty()) return; // Check if the list is empty

        // Loop through all the selected images
        for (Uri imageUri : uploadedImages) {
            // Create a unique reference for each image using the tipId and timestamp
            String imageName = tipId + "_" + System.currentTimeMillis() + "_skincare_tips_image.jpg";
            StorageReference photoRef = storageReference.child(imageName);

            UploadTask uploadTask = photoRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL of the uploaded image
                photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save the image URL under the corresponding tipId in the "SkincareTips" node
                    skincareTipsDatabaseReference.child(tipId).child("images").push().setValue(uri.toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Image uploaded and URL saved successfully: " + uri.toString());
                                } else {
                                    Log.e(TAG, "Failed to save image URL: " + task.getException().getMessage());
                                }
                            });
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload image: " + e.getMessage());
            });
        }
    }

}