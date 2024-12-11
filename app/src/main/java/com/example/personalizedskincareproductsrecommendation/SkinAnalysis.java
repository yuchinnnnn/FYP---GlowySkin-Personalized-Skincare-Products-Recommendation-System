package com.example.personalizedskincareproductsrecommendation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.Interpreter;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinAnalysis extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final int CAMERA_PERMISSION_CODE = 100; //A constant to identify permission request
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageButton capture, upload;
    private ImageView back;
    private FaceOutlineView faceOutlineView;
    private Interpreter skinConditionModel;
    private Interpreter skinTypeModel;
    private String userId, keyId, skinType;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private float[] skinConditionPercentages = new float[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_analysis);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra("ARG_USER_ID");
        Log.d("SkinAnalysis", "User ID: " + userId);
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("SkinAnalysisImages/" + userId);
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinAnalysis").child(userId);

        capture = findViewById(R.id.capture_button);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

        upload = findViewById(R.id.upload_button);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        // Initialize the FrameLayout to show camera preview
        FrameLayout cameraFrame = findViewById(R.id.cameraFrame);
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this); // Add callback for SurfaceHolder
        cameraFrame.addView(surfaceView);

        faceOutlineView = new FaceOutlineView(this, null);
        cameraFrame.addView(faceOutlineView);

        // Request Camera Permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }

        back = findViewById(R.id.back);
        // Handle back button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Go back to the previous activity
            }
        });

        showSkinAnalysisTipsDialog();
        skinConditionModel = loadSkinConditionModel();
        skinTypeModel = loadSkinTypeModel();
    }

    private void analyzeSkin(Bitmap bitmap) {
        // Preprocess the image
        Bitmap preprocessedImage = preprocessImage(bitmap);

        // Convert the image to ByteBuffer for model input
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(preprocessedImage);

        // Run the skin condition model
        float[][] skinConditionResult = new float[1][6];  // Assuming 6 output categories
        skinConditionModel.run(inputBuffer, skinConditionResult);

        // Apply softmax to get percentages for each skin condition
        skinConditionPercentages = softmax(skinConditionResult[0]); // Set the instance variable

        // Run the skin type model
        float[][] skinTypeResult = new float[1][3];  // Assuming 3 output categories
        skinTypeModel.run(inputBuffer, skinTypeResult);

        // Determine combination or sensitive skin based on conditions
        skinType = interpretSkinTypeResult(skinTypeResult, skinConditionPercentages); // Set the instance variable


        displayResultsWithPercentages(skinConditionPercentages, skinType, userId, keyId);
    }

    private void displayResultsWithPercentages(float[] skinConditionPercentages, String skinType, String userId, String keyId) {
        // Build the result text
        String[] skinConditionLabels = {"Acne", "Dark Circle", "Dark Spots", "Pores", "Redness", "Wrinkles"};
        StringBuilder resultText = new StringBuilder();

        for (int i = 0; i < skinConditionPercentages.length; i++) {
            resultText.append("• ")
                    .append(skinConditionLabels[i])
                    .append(": ")
                    .append(String.format("%d", (int) skinConditionPercentages[i]))
                    .append("%\n");
        }

        resultText.append("\n").append("Skin Type: ").append(skinType);
        resultText.append("\n\n").append("Loading to skin analysis result page...");

        // Call the method to display the dialog with prepared text
        showSkinAnalysisDialog(resultText.toString(), userId, keyId);
    }


    private void showSkinAnalysisDialog(String resultText, String userId, String keyId) {
        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_skin_analysis, null);

        // Set the content text with the spannable string
        TextView dialogContent = dialogView.findViewById(R.id.dialogContent);
        dialogContent.setText(resultText);

        // Create the SweetAlertDialog and set the custom view
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(SkinAnalysis.this, SweetAlertDialog.SUCCESS_TYPE)
                .setCustomView(dialogView)
                .hideConfirmButton();

        sweetAlertDialog.show();
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap, String userId, String skinType, float[] skinConditionPercentages) {
        // Convert the bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Compress the image to JPEG format
        byte[] imageData = baos.toByteArray();

        // Define the storage reference
        String imageName = userId + "_" + System.currentTimeMillis() + ".jpg"; // Unique image name
        StorageReference imageRef = storageReference.child("SkinAnalysisImages/" + userId + "/" + userId + imageName);

        // Upload the image as a byte array
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get the download URL after successful upload
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                // Now save the URL to Firebase Realtime Database
                saveImageUrlToDatabase(downloadUrl, userId, skinType, skinConditionPercentages); // Pass the skinType and percentages
            });
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            showErrorMessage("Upload failed: " + exception.getMessage());
        });
    }

    public String getCurrentDateTime() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define a date time format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the current date and time
        String currentDateTime = now.format(formatter);

        return currentDateTime;
    }

    private void saveImageUrlToDatabase(String imageUrl, String userId, String skinType, float[] skinConditionPercentages) {
        Log.d("RealtimeDatabase", "Saving data: " + imageUrl + ", userId: " + userId + ", skinType: " + skinType);

        // Initialize Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Generate a new unique keyId for the analysis
        String keyId = database.child("SkinAnalysis").child(userId).push().getKey();

        // Check if keyId was generated successfully
        if (keyId == null) {
            showErrorMessage("Error generating key for skin analysis");
            return;
        }

        // Create a HashMap to store image data
        Map<String, Object> skinAnalysisData = new HashMap<>();
        skinAnalysisData.put("userId", userId);
        skinAnalysisData.put("imageUrl", imageUrl);
        skinAnalysisData.put("uploadedDateTime", getCurrentDateTime());
        skinAnalysisData.put("skinType", skinType);

        Map<String, Object> results = new HashMap<>();
        results.put("acne", skinConditionPercentages[0]);
        results.put("redness", skinConditionPercentages[1]);
        results.put("wrinkles", skinConditionPercentages[2]);
        results.put("darkSpot", skinConditionPercentages[3]);
        results.put("darkCircle", skinConditionPercentages[4]);
        results.put("pores", skinConditionPercentages[5]);

        skinAnalysisData.put("skinCondition", results);

        // Save image URL and results under the specific user and keyId
        database.child("SkinAnalysis").child(userId).child(keyId)
                .setValue(skinAnalysisData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("RealtimeDatabase", "Data successfully written!");

                    // Navigate to SkinAnalysisResult once data is saved
                    Intent intent = new Intent(this, SkinAnalysisResult.class);
                    intent.putExtra("ARG_USER_ID", userId);
                    intent.putExtra("ARG_ANALYSIS_ID", keyId);  // Pass the generated keyId here
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("RealtimeDatabase", "Error writing data", e);
                    showErrorMessage("Error saving to database: " + e.getMessage());
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopPreview();
            camera.release(); // Release the camera
            camera = null; // Avoid memory leaks
        }
        if (skinConditionModel != null) {
            skinConditionModel.close();
        }
        if (skinTypeModel != null) {
            skinTypeModel.close();
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3); // Assuming RGB image 224x224
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[224 * 224];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int pixel : pixels) {
            // Normalize pixel values (e.g., 0..255 to -1..1)
            byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f); // Red
            byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);  // Green
            byteBuffer.putFloat((pixel & 0xFF) / 255.0f);         // Blue
        }
        return byteBuffer;
    }

    private float[] softmax(float[] input) {
        float sum = 0f;
        for (float val : input) {
            sum += Math.exp(val);
        }
        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (float) (Math.exp(input[i]) / sum) * 100; // Convert to percentage
        }
        return output;
    }

    private void captureImage() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // Generate a new keyId each time an image is captured
                keyId = databaseReference.push().getKey(); // Generate unique keyId

                // Save the image to device storage
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                // Analyze skin to get skin type and skin condition percentages
                analyzeSkin(bitmap); // This sets skinType and skinConditionPercentages

                // Now call uploadImageToFirebaseStorage with the skinType and skinConditionPercentages
                uploadImageToFirebaseStorage(bitmap, userId, skinType, skinConditionPercentages);
            }
        });
    }

    private Bitmap preprocessImage(Bitmap bitmap) {
        // Resize the image to the input size of the model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false); // assuming model expects 224x224 input
        // Optionally convert to grayscale if your model requires it
        // Normalize pixel values if needed
        return resizedBitmap;
    }

    // Handle camera permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Handle permission denial
            }
        }
    }

    // Method to show a dialog with tips for skin analysis
    private void showSkinAnalysisTipsDialog() {
        SweetAlertDialog dialog = new SweetAlertDialog(SkinAnalysis.this, SweetAlertDialog.NORMAL_TYPE);
        dialog.setTitleText("Skin Analysis Tips");

        // Customize the message text for line breaks
        String message = "1. Make sure your face is well lit.<br>" +
                "2. Avoid shadows on your face.<br>" +
                "3. Keep the camera steady.<br>" +
                "4. Remove any makeup for the best results.";

        dialog.setContentText(message);
        dialog.setConfirmText("Got it!");

        dialog.show();
    }

    private String interpretSkinTypeResult(float[][] skinTypeResult, float[] skinConditionPercentages) {
        String[] skinTypeLabels = {"Oily", "Dry", "Normal"};
        int maxIndex = 0;
        for (int i = 1; i < skinTypeResult[0].length; i++) {
            if (skinTypeResult[0][i] > skinTypeResult[0][maxIndex]) {
                maxIndex = i;
            }
        }
        String baseSkinType = skinTypeLabels[maxIndex];

        // Check for combination skin (both oily and dry skin present)
        if (skinTypeResult[0][0] > 0.5 && skinTypeResult[0][1] > 0.5) {
            return "Combination Skin";
        }

        // Check for sensitive skin (if redness percentage is high)
        if (skinConditionPercentages[4] > 50) {  // Index 4 represents redness
            return "Sensitive Skin";
        }

        return baseSkinType;
    }


    // Open Camera
    // Open the front camera
    private void openCamera() {
        int cameraId = findFrontFacingCamera(); // Find the ID for the front camera
        if (cameraId != -1) {
            camera = Camera.open(cameraId); // Open the front camera
            setCameraDisplayOrientation(cameraId); // Set the camera orientation
        } else {
            camera = Camera.open(); // Fallback to default camera (usually the back camera)
        }
    }

    // Method to set the correct camera orientation
    private void setCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // Compensate for the mirror effect
        } else {  // Back-facing camera
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }


    // Method to find the front-facing camera
    private int findFrontFacingCamera() {
        int cameraCount = Camera.getNumberOfCameras(); // Get the total number of cameras
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i; // Return the ID of the front-facing camera
            }
        }
        return -1; // Return -1 if no front camera is found
    }

    // SurfaceHolder callback methods
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        // Stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // Ignore: tried to stop a non-existent preview
        }

        // Restart camera preview with new settings
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    private void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }


    // Load the TFLite model for skin condition
    private Interpreter loadSkinConditionModel() {

        try {
            AssetFileDescriptor fileDescriptor = this.getAssets().openFd("skin_condition_MobileNetV2.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer modelFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            return new Interpreter(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Load the TFLite model for skin type
    private Interpreter loadSkinTypeModel() {
        try {
            AssetFileDescriptor fileDescriptor = this.getAssets().openFd("skin_type_MobileNetV2.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer modelFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            return new Interpreter(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                // Get the URI of the selected image
                Uri imageUri = data.getData();

                // Convert URI to Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Generate a new keyId for each uploaded image
                keyId = databaseReference.push().getKey();

                // Analyze skin to get skin type and skin condition percentages
                analyzeSkin(bitmap); // This sets skinType and skinConditionPercentages

                // Call uploadImageToFirebaseStorage with the skinType and skinConditionPercentages
                uploadImageToFirebaseStorage(bitmap, userId, skinType, skinConditionPercentages);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showErrorMessage(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .show();
    }
}
