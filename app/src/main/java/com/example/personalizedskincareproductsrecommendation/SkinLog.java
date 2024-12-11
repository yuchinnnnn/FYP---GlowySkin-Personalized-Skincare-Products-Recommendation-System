package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinLog extends AppCompatActivity {
    // Constants for image requests and permissions
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    // Image URIs
    private Uri photoUri, leftUri, frontUri, rightUri, neckUri;

    // UI Elements
    private ImageView back, left_selfie, front_selfie, right_selfie, neck_selfie;
    private View frontView, leftView, rightView, neckView;
    private Button done;

    // Firebase references
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId, currentImageType;

    // Initialize Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_log);

        // Initialize UI elements
        back = findViewById(R.id.back);
        left_selfie = findViewById(R.id.left_selfie);
        front_selfie = findViewById(R.id.front_selfie);
        right_selfie = findViewById(R.id.right_selfie);
        neck_selfie = findViewById(R.id.neck_selfie);

//        leftView = findViewById(R.id.leftFaceOutlineView);
//        frontView = findViewById(R.id.frontFaceOutlineView);
//        rightView = findViewById(R.id.rightFaceOutlineView);
//        neckView = findViewById(R.id.neckOutlineView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra("ARG_USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("images/" + userId);
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinLog").child(userId);

        // Set click listeners
        back.setOnClickListener(v -> {
            finish();
        });

        done = findViewById(R.id.done_button);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateImage()){
                    saveImageDialog();
                } else {
                    SweetAlertDialog dialog = new SweetAlertDialog(SkinLog.this, SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText("Error");
                    dialog.setContentText("Please take all the selfies.");
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setConfirmButton("OK", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    });
                }
            }
        });

        left_selfie.setOnClickListener(v -> {
            showImagePickerDialog("left_selfie");
//            leftView.setVisibility(View.VISIBLE); // Show outline for left selfie
        });

        front_selfie.setOnClickListener(v -> {
            showImagePickerDialog("front_selfie");
//            frontView.setVisibility(View.VISIBLE); // Show outline for left selfie
        });

        right_selfie.setOnClickListener(v -> {
            showImagePickerDialog("right_selfie");
//            rightView.setVisibility(View.VISIBLE); // Show outline for left selfie
        });

        neck_selfie.setOnClickListener(v -> {
            showImagePickerDialog("neck_selfie");
//            neckView.setVisibility(View.VISIBLE); // Show outline for left selfie
        });
        // Request necessary permissions
        requestPermissions();
    }

    // Request camera and storage permissions
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // Show image picker dialog for selecting an image source
    private void showImagePickerDialog(String imageType) {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showImageQualityAlertDialog(imageType);
                    } else if (which == 1) {
                        openGallery(imageType);
                    }
                });
        builder.show();
    }

    // Alert dialog to remind user about image quality requirements
    private void showImageQualityAlertDialog(String imageType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ensure Image Quality")
                .setMessage("To ensure the quality of the image, please ensure the following:\n" +
                        "1. Make sure you are facing the correct direction.\n" +
                        "2. Use good lighting.\n" +
                        "3. Ensure your face is clearly visible.")
                .setPositiveButton("Proceed", (dialog, which) -> openCamera(imageType))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Open camera to capture an image
    private void openCamera(String imageType) {
        currentImageType = imageType; // Set currentImageType before launching the camera
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
            }
        }
    }

    // Open gallery to select an image
    private void openGallery(String imageType) {
        currentImageType = imageType; // Set currentImageType before launching the gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Create a temporary file for storing the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                setImageToView(photoUri);
                uploadImageToFirebase(photoUri);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImageUri = data.getData();
                setImageToView(selectedImageUri);
                uploadImageToFirebase(selectedImageUri);
            }
        }
    }

    // Set captured/selected image to the corresponding ImageView
    private void setImageToView(Uri uri) {
        switch (currentImageType) {
            case "left_selfie":
                leftUri = uri;
                Glide.with(this).load(uri).into(left_selfie);
                break;
            case "front_selfie":
                frontUri = uri;
                Glide.with(this).load(uri).into(front_selfie);
                break;
            case "right_selfie":
                rightUri = uri;
                Glide.with(this).load(uri).into(right_selfie);
                break;
            case "neck_selfie":
                neckUri = uri;
                Glide.with(this).load(uri).into(neck_selfie);
                break;
        }
    }

    // Upload the selected image to Firebase Storage
    private void uploadImageToFirebase(Uri uri) {
        String fileName = "selfie_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child(fileName);
        fileRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                switch (currentImageType) {
                    case "left_selfie":
                        leftUri = downloadUri;
                        break;
                    case "front_selfie":
                        frontUri = downloadUri;
                        break;
                    case "right_selfie":
                        rightUri = downloadUri;
                        break;
                    case "neck_selfie":
                        neckUri = downloadUri;
                        break;
                }
//                Toast.makeText(SkinLog.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> Toast.makeText(SkinLog.this, "Failed to get download URL", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(SkinLog.this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private boolean validateImage(){
        boolean isValid = true;

        // Check if category is selected
        if (left_selfie.toString().isEmpty()) {
            isValid = false;
            Toast.makeText(this, "Please select a selfie", Toast.LENGTH_SHORT).show();
        }
        if (front_selfie.toString().isEmpty()) {
            isValid = false;
            Toast.makeText(this, "Please select a selfie", Toast.LENGTH_SHORT).show();
        }
        if (right_selfie.toString().isEmpty()) {
            isValid = false;
            Toast.makeText(this, "Please select a selfie", Toast.LENGTH_SHORT).show();
        }
        if (neck_selfie.toString().isEmpty()) {
            isValid = false;
            Toast.makeText(this, "Please select a selfie", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    // Show a dialog to confirm saving images
    private void saveImageDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Once saved, you cannot change the images.")
                .setConfirmText("Yes!")
                .setCancelText("No")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    if(!isUpload()){
                        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Image Required")
                                .setContentText("Please upload all the photos before saving.")
                                .setConfirmText("OK")
                                .show();
                        return;
                    }
                    saveAllImages();
                })
                .setCancelClickListener(SweetAlertDialog::dismiss)
                .show();
    }

    private boolean isUpload() {
        return leftUri != null && frontUri != null && rightUri != null && neckUri != null;
    }

    // Save all selfies in the database
    private void saveAllImages() {
        if (leftUri == null || frontUri == null || rightUri == null || neckUri == null) {
            Toast.makeText(this, "Please capture all selfies before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new log ID
        String logId = databaseReference.push().getKey();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a map to hold the data for the new structure
        SkinLogData.Selfies selfies = new SkinLogData.Selfies(leftUri.toString(), rightUri.toString(), frontUri.toString(), neckUri.toString());

        // Create a map to hold the skin log entry
        HashMap<String, Object> skinLogEntry = new HashMap<>();
        skinLogEntry.put("userId", userId);
        skinLogEntry.put("timestamp", timestamp);
        skinLogEntry.put("selfies", selfies);

        // Save the SkinLogData object to Firebase under the new logId
        databaseReference.child(logId).setValue(skinLogEntry)
                .addOnSuccessListener(aVoid -> {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SkinLog.this, SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setTitle("Skin Log Saved");
                    sweetAlertDialog.setContentText("Skin log is saved successfully");
                    sweetAlertDialog.setConfirmText("OK");
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            finish();
                        }
                    });
                    sweetAlertDialog.show();
                    clearImageViews();
                })
                .addOnFailureListener(e -> Toast.makeText(SkinLog.this, "Failed to save skin log", Toast.LENGTH_SHORT).show());
    }

    // Clear image views after saving
    private void clearImageViews() {
        left_selfie.setImageResource(0);
        front_selfie.setImageResource(0);
        right_selfie.setImageResource(0);
        neck_selfie.setImageResource(0);
        leftUri = null;
        frontUri = null;
        rightUri = null;
        neckUri = null;
    }
}
