package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SkinLog extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Uri photoUri;

    private ImageView back, left_selfie, front_selfie, right_selfie, neck_selfie;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;
    private static final String ARG_USER_ID = "userId";

    // Initialize Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_log);

        // Initialize UI elements
        back = findViewById(R.id.back_button);
        left_selfie = findViewById(R.id.left_selfie);
        front_selfie = findViewById(R.id.front_selfie);
        right_selfie = findViewById(R.id.right_selfie);
        neck_selfie = findViewById(R.id.neck_selfie);

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("images/" + userId);

        databaseReference = FirebaseDatabase.getInstance().getReference("SkinLog").child(userId);

        back.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });

        left_selfie.setOnClickListener(v -> showImagePickerDialog("left_selfie"));
        front_selfie.setOnClickListener(v -> showImagePickerDialog("front_selfie"));
        right_selfie.setOnClickListener(v -> showImagePickerDialog("right_selfie"));
        neck_selfie.setOnClickListener(v -> showImagePickerDialog("neck_selfie"));

        // Request necessary permissions
        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void showImagePickerDialog(String imageType) {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Take photo
                        openCamera(imageType);
                    } else if (which == 1) {
                        // Pick from gallery
                        openGallery(imageType);
                    }
                });
        builder.show();
    }

    private void openCamera(String imageType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create a file to save the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(SkinLog.this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.personalizedskincareproductsrecommendation.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(SkinLog.this, "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery(String imageType) {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoUri != null) {
                    setImageViewAndUpload("left_selfie", photoUri); // Change based on image type
                } else {
                    Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    setImageViewAndUpload("left_selfie", selectedImage); // Change based on image type
                }
            }
        } else {
            Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageViewAndUpload(String imageType, Uri imageUri) {
        switch (imageType) {
            case "left_selfie":
                left_selfie.setImageURI(imageUri);
                break;
            case "front_selfie":
                front_selfie.setImageURI(imageUri);
                break;
            case "right_selfie":
                right_selfie.setImageURI(imageUri);
                break;
            case "neck_selfie":
                neck_selfie.setImageURI(imageUri);
                break;
        }
        uploadImageToFirebase(imageUri, imageType);
    }

    private void uploadImageToFirebase(Uri imageUri, String imageType) {
        if (imageUri != null) {
            // Create a reference to 'images/userId/imageType.jpg'
            StorageReference fileReference = storageReference.child(imageType + ".jpg");

            // Upload the image to Firebase Storage
            UploadTask uploadTask = fileReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the download URL after the image is uploaded
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Store the download URL in Firebase Realtime Database
                    saveImageUrlToDatabase(uri.toString(), imageType);
                }).addOnFailureListener(e -> {
                    Toast.makeText(SkinLog.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SkinLog", "Error getting download URL", e);
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(SkinLog.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("SkinLog", "Error uploading image", e);
            });
        } else {
            Toast.makeText(SkinLog.this, "Image URI is null, cannot upload image", Toast.LENGTH_SHORT).show();
            Log.e("SkinLog", "Image URI is null");
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Log the storage directory path for debugging
        Log.d("SkinLog", "Storage Directory: " + storageDir.getAbsolutePath());

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Log the created image path for debugging
        Log.d("SkinLog", "Image File Path: " + image.getAbsolutePath());

        return image;
    }

    private void saveImageUrlToDatabase(String imageUrl, String imageType) {
        // Save the image URL to the Firebase Realtime Database
        databaseReference.child(imageType).setValue(imageUrl);
    }
}

