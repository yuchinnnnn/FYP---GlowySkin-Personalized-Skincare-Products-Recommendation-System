package com.example.personalizedskincareproductsrecommendation;

import static com.example.personalizedskincareproductsrecommendation.AdminAddContent.ARG_USER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdminAddProducts extends AppCompatActivity {

    private TextInputLayout categoryLayout, typeLayout, nameLayout, functionLayout, afterUseLayout, ingredientLayout;
    private AutoCompleteTextView categoryText, typeText;
    private EditText name, brand, function, afterUse, ingredient;
    private ImageView uploadedImage, cancelCoverImage, back;
    private LinearLayout uploadImageButton, uploadedImageLayout;
    private Button addButton, cancelButton;
    private String userId;
    private Uri imageUri;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_products);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        // Initialize Firestore and Storage
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("product_images");

        // Initialize UI components
        initializeUI();

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminAddProducts.this, AdminManageProducts.class);
                intent.putExtra(ARG_USER_ID, userId);
                startActivity(intent);
                finish();
            }
        });

        // Set up the upload image button click listener
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Handle the add product button click
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateProductDetails()) {
                    showConfirmationDialog();  // Show confirmation only if validation passes
                } else {
                    SweetAlertDialog dialog = new SweetAlertDialog(AdminAddProducts.this, SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText("Error");
                    dialog.setContentText("Please fill in all the required fields.");
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

        // Handle the cancel button click
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity
            }
        });
    }

    private void initializeUI() {
        categoryLayout = findViewById(R.id.category);
        typeLayout = findViewById(R.id.type);
        nameLayout = findViewById(R.id.name_layout);
        functionLayout = findViewById(R.id.function_layout);
        afterUseLayout = findViewById(R.id.afterUse_layout);
        ingredientLayout = findViewById(R.id.ingredient_layout);

        categoryText = findViewById(R.id.category_text);
        String[] categories = new String[]{"Acne Treatment", "Anti-Aging Skin Care", "Brightening",
                "Dryness Control", "Oily Skin Care", "Pore Care", "Reduce Spots",
                "Sensitive Skin Care"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryText.setAdapter(adapter);

        typeText = findViewById(R.id.type_text);
        String[] types = new String[]{"Toner", "Face Cleanser", "Facial Treatment", "Serum" ,"General Moisturizer",
                "Sunscreen" ,"Exfoliator", "Makeup Remover" ,"Day Moisturizer",
                "Eye Moisturizer" ,"Wet Mask", "Emulsion", "Night Moisturizer", "Sheet Mask"
                ,"Oil", "Essence" ,"Overnight Mask" ,"Eye Mask"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        typeText.setAdapter(typeAdapter);

        name = findViewById(R.id.name);
        brand = findViewById(R.id.brand);
        function = findViewById(R.id.function);
        afterUse = findViewById(R.id.afterUse);
        ingredient = findViewById(R.id.ingredient);

        uploadedImage = findViewById(R.id.uploadedImage);
        cancelCoverImage = findViewById(R.id.cancel_cover_image);

        uploadImageButton = findViewById(R.id.upload_image_button);
        uploadedImageLayout = findViewById(R.id.uploaded_image);

        addButton = findViewById(R.id.AddButton);
        cancelButton = findViewById(R.id.CancelButton);
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Product Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadedImage.setImageURI(imageUri);
        }
    }

    private boolean validateProductDetails() {
        boolean isValid = true;

        // Check if category is selected
        if (categoryText.getText().toString().isEmpty()) {
            categoryLayout.setError("Please select a category");
            isValid = false;
        } else {
            categoryLayout.setError(null);
        }

        // Check if type is selected
        if (typeText.getText().toString().isEmpty()) {
            typeLayout.setError("Please select a type");
            isValid = false;
        } else {
            typeLayout.setError(null);
        }

        // Check if product name is provided
        if (name.getText().toString().isEmpty()) {
            nameLayout.setError("Product name is required");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }

        // Check if brand name is provided
        if (brand.getText().toString().isEmpty()) {
            brand.setError("Brand name is required");
            isValid = false;
        } else {
            brand.setError(null);
        }

        // Check if function is provided
        if (function.getText().toString().isEmpty()) {
            functionLayout.setError("Function description is required");
            isValid = false;
        } else {
            functionLayout.setError(null);
        }

        // Check if after-use description is provided
        if (afterUse.getText().toString().isEmpty()) {
            afterUseLayout.setError("Please provide after-use description");
            isValid = false;
        } else {
            afterUseLayout.setError(null);
        }

        // Check if ingredient information is provided
        if (ingredient.getText().toString().isEmpty()) {
            ingredientLayout.setError("Please provide ingredients information");
            isValid = false;
        } else {
            ingredientLayout.setError(null);
        }

        return isValid;
    }

    private void showConfirmationDialog() {
        SweetAlertDialog dialog = new SweetAlertDialog(AdminAddProducts.this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("Are you sure?");
        dialog.setContentText("Your products will be uploaded once you confirm.");
        dialog.show();
        dialog.setCancelable(false);
        dialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                saveProductToFirestore();
            }
        });
        dialog.setCancelText("No");
    }

    private void saveProductToFirestore() {
        if (imageUri != null) {
            final String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child(imageName + ".jpg");

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveProductDetails(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdminAddProducts.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            saveProductDetails(null);
        }
    }

    private void saveProductDetails(String imageUrl) {
//        String productId =
        String productBrand = brand.getText().toString();
        String productName = name.getText().toString();
        String productFunction = function.getText().toString();
        String afterUseText = afterUse.getText().toString();
        String ingredients = ingredient.getText().toString();
        String category = categoryText.getText().toString();
        String type = typeText.getText().toString();


        Map<String, Object> product = new HashMap<>();
//        product.put("id", productId);
        product.put("brand", productBrand);
        product.put("name", productName);
        product.put("function", productFunction);
        product.put("afterUse", afterUseText);
        product.put("ingredients", ingredients);
        product.put("category", category);
        product.put("type", type);
        product.put("image_url", imageUrl);

        firestore.collection("skin_care_product")
                .orderBy("name")  // Order by any field that ensures correct order; here, we're ordering by "name"
                .limitToLast(1)  // Limit the query to get only the last added product
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Get the last document and extract the ID
                                DocumentSnapshot lastDocument = querySnapshot.getDocuments().get(0);
                                String lastProductId = lastDocument.getId();

                                // Generate the next product ID by extracting the numeric part of the last ID
                                int lastProductNumber = Integer.parseInt(lastProductId.replaceAll("[^0-9]", "")); // Extract number from product_ID1234
                                int newProductNumber = lastProductNumber + 1;
                                String newProductId = "product_" + newProductNumber;
                                Log.d("NEW ADDED PRODUCT", "New product ID: " + newProductId);

                                // Now save the product with the new product ID
                                firestore.collection("skin_care_product")
                                        .document(newProductId)  // Set the new dynamic product ID
                                        .set(product)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    SweetAlertDialog sDialog = new SweetAlertDialog(AdminAddProducts.this, SweetAlertDialog.SUCCESS_TYPE);
                                                    sDialog.setTitleText("Successfully");
                                                    sDialog.setContentText("Your product has been added successfully");
                                                    sDialog.setConfirmText("Ok");
                                                    sDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sDialog.dismiss(); // Dismiss the dialog
                                                            finish(); // Close the activity after dismissing the dialog
                                                        }
                                                    });
                                                    sDialog.show();
                                                } else {
                                                    Toast.makeText(AdminAddProducts.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // If no products exist, you can start with an ID like product_1
                                String newProductId = "product_0";
                                firestore.collection("skin_care_product")
                                        .document(newProductId)
                                        .set(product)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(AdminAddProducts.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                                    finish(); // Close the activity
                                                } else {
                                                    Toast.makeText(AdminAddProducts.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(AdminAddProducts.this, "Error fetching last product", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
