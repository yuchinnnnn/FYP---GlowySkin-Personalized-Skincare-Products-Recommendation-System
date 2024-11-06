package com.example.personalizedskincareproductsrecommendation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class AdminEditProducts extends AppCompatActivity {

    private String userId, productId, imageUrl;
    private ImageView back, delete, productImage;
    private EditText name, afterUse, brand, ingredients, function;
    private AutoCompleteTextView type;
    private Button save;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference productImageRef;
    private Uri imageUri;
    private CardView uploadImageButton;

    // Hardcoded array for product types
    private static final String[] PRODUCT_TYPES = new String[]
            {"Toner", "Face Cleanser", "Facial Treatment", "Serum" ,"General Moisturizer",
                    "Sunscreen" ,"Exfoliator", "Makeup Remover" ,"Day Moisturizer",
                    "Eye Moisturizer" ,"Wet Mask", "Emulsion", "Night Moisturizer", "Sheet Mask"
                    ,"Oil", "Essence" ,"Overnight Mask" ,"Eye Mask"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_products);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI elements
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEditProducts.this, AdminManageProducts.class);
            intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
            startActivity(intent);
        });

        delete = findViewById(R.id.delete);
        delete.setOnClickListener(v -> deleteProduct());

        productImage = findViewById(R.id.uploadedImage);

        name = findViewById(R.id.edit_name);
        afterUse = findViewById(R.id.edit_afterUse);
        brand = findViewById(R.id.edit_brand);
        ingredients = findViewById(R.id.edit_ingredients);
        function = findViewById(R.id.edit_function);
        save = findViewById(R.id.save_button);
        type = findViewById(R.id.hint_text);

        // Set up dropdown with hardcoded product types
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PRODUCT_TYPES);
        type.setAdapter(typeAdapter);

        // Retrieve product details from intent
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("productId");
            String productName = intent.getStringExtra("productName");
            String productBrand = intent.getStringExtra("productBrand");
            String productType = intent.getStringExtra("productType");
            String productIngredients = intent.getStringExtra("productIngredients");
            String productFunction = intent.getStringExtra("productFunction");
            String productUsage = intent.getStringExtra("productAfterUse");
            String imageUrl = intent.getStringExtra("image_url");
            Log.d("AdminEditProducts", "Product details: " + productId + productName + ", " + productBrand + ", " + productType
                    + ", " + productIngredients + ", " + productFunction + ", " + productUsage + ", " + imageUrl);

            brand.setText(productBrand);
            name.setText(productName);
            afterUse.setText(productUsage);
            type.setText(productType);
            ingredients.setText(productIngredients);
            function.setText(productFunction);

            // Load the initial image URL
            Glide.with(this).load(imageUrl).into(productImage);
        }

        uploadImageButton = findViewById(R.id.upload_image);
        uploadImageButton.setOnClickListener(v -> selectImage());

        save.setOnClickListener(v -> saveProduct());
    }

    // Select image from gallery
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();

                    // Display the selected image in the ImageView immediately using setImageURI
                    productImage.setImageURI(imageUri);

                    Log.d("AdminEditProducts", "Image selected: " + imageUri);
                }
            });

    private void saveProduct() {
        String productName = name.getText().toString();
        String productBrand = brand.getText().toString();
        String productType = type.getText().toString();
        String productIngredients = ingredients.getText().toString();
        String productFunction = function.getText().toString();
        String productUsage = afterUse.getText().toString();

        if (imageUri != null) {
            // Define the Firebase Storage reference
            productImageRef = storage.getReference("product_images/" + productName + ".jpg");

            // Upload the image file
            productImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> productImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrl = uri.toString(); // Get the URL of the uploaded image

                        // Load the image into the ImageView using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(productImage);
                        }

                        // Save product details, including the image URL, to Firestore
                        saveProductToFirestore(productName, productBrand, productType, productIngredients, productFunction, productUsage, imageUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(AdminEditProducts.this, "Image upload failed.", Toast.LENGTH_SHORT).show());
        } else {
            // Save the product details without an image URL if no image is selected
            saveProductToFirestore(productName, productBrand, productType, productIngredients, productFunction, productUsage, imageUrl);
        }
    }


    private void saveProductToFirestore(String productName, String productBrand, String productType,
                                        String productIngredients, String productFunction,
                                        String productUsage, String imageUrl) {

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing.", Toast.LENGTH_SHORT).show();
            return; // Exit the function if productId is null or empty
        }

        // Create a product map with the provided data
        Map<String, Object> productData = new HashMap<>();
        productData.put("id", productId);
        productData.put("name", productName);
        productData.put("brand", productBrand);
        productData.put("type", productType);
        productData.put("ingredients", productIngredients);
        productData.put("function", productFunction);
        productData.put("usage", productUsage);
        productData.put("image_url", imageUrl);

        // Reference the Firestore document with the given product ID
        db.collection("skin_care_product").document(productId)
                .set(productData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product saved successfully.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save product: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteProduct() {
        // Delete product from Firestore
        db.collection("skin_care_product").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Delete image from Firebase Storage
                        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
                        imageRef.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(AdminEditProducts.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(AdminEditProducts.this, "Failed to delete product image", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(AdminEditProducts.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AdminEditProducts.this, "Failed to delete product", Toast.LENGTH_SHORT).show());
    }
}
