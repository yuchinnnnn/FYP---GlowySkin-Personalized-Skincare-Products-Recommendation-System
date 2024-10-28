package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinAnalysisResult extends AppCompatActivity {

    private TextView skinTypeResult;
    private TextView skinConditionResult;
    private RecyclerView recommendedProductsRecyclerView;
    private ImageView back, info;
    private String userId, keyId, skinType;
    private FirebaseFirestore firestore;
    private RecommendedProductsAdapter recommendedProductsAdapter;
    private ArrayList<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_analysis_result);

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });

        info = findViewById(R.id.infoIcon);
        info.setOnClickListener(v -> {
            // Create a SweetAlertDialog
            SweetAlertDialog dialog = new SweetAlertDialog(SkinAnalysisResult.this, SweetAlertDialog.NORMAL_TYPE);
            dialog.setTitleText("Information");

            // Set the content text based on skin type
            String message;
            switch (skinType) {
                case "Dry":
                    message = "Dry skin may feel tight, rough, and may have flakiness. It's important to hydrate regularly.";
                    break;
                case "Oily":
                    message = "Oily skin can appear shiny and is more prone to acne. Use lightweight, non-comedogenic products.";
                    break;
                case "Combination":
                    message = "Combination skin has both dry and oily areas. Tailor your skincare routine accordingly.";
                    break;
                case "Sensitive":
                    message = "Sensitive skin may react to products. Choose gentle, fragrance-free options.";
                    break;
                case "Normal":
                    message = "Normal skin is balanced and requires regular maintenance. Keep up with a good skincare routine.";
                    break;
                default:
                    message = "Your skin type is based on your skin condition.";
                    break;
            }

            // Set the message and show the dialog
            dialog.setContentText(message);
            dialog.show();
        });

        // Retrieve userId and keyId from the Intent
        userId = getIntent().getStringExtra("ARG_USER_ID");
        keyId = getIntent().getStringExtra("ARG_ANALYSIS_ID");

        // Log to check if userId and keyId are correctly retrieved
        Log.d("SkinAnalysisResult", "Retrieved userId: " + userId + ", keyId: " + keyId);

        if (userId == null || keyId == null) {
            Log.e("Intent Error", "userId or keyId is null. Check intent data passing.");
            return; // Stop execution if userId or keyId is null
        }

        // Initialize views
        skinTypeResult = findViewById(R.id.skinTypeResult);
        skinConditionResult = findViewById(R.id.skinConditionResult);
        recommendedProductsRecyclerView = findViewById(R.id.recommendedProductsRecyclerView);

        recommendedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        firestore = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        recommendedProductsAdapter = new RecommendedProductsAdapter(this, productList);
        recommendedProductsRecyclerView.setAdapter(recommendedProductsAdapter);

        String userFunction = "Pore Care"; // Example function to filter by
        List<String> afterUses = new ArrayList<>();
        afterUses.add("Hydrating");
        afterUses.add("Good For Oily Skin");
        afterUses.add("Reduces Large Pores");
        // Load recommended products
        loadRecommendedProducts();

        // Load skin analysis data
        loadSkinAnalysisData();
    }

    private void loadSkinAnalysisData() {
        // Initialize Firebase Realtime Database reference
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Reference to the specific user's skin analysis
        database.child("SkinAnalysis").child(userId).child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get data from snapshot
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    skinType = snapshot.child("skinType").getValue(String.class); // Assign to class variable

                    // Retrieve skin condition data
                    HashMap<String, Float> skinCondition = new HashMap<>();
                    for (DataSnapshot conditionSnapshot : snapshot.child("skinCondition").getChildren()) {
                        skinCondition.put(conditionSnapshot.getKey(), conditionSnapshot.getValue(Float.class));
                    }

                    // Update UI
                    displayAnalysisImage(imageUrl);
                    skinTypeResult.setText(skinType);
                    skinConditionResult.setText(getSkinConditionResults(skinCondition));
                } else {
                    Log.d("Firebase Error", "No data found for userId: " + userId + " and keyId: " + keyId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase Error", "Failed to read value.", error.toException());
            }
        });
    }

    private String getSkinConditionResults(HashMap<String, Float> skinConditions) {
        StringBuilder results = new StringBuilder();
        for (String condition : skinConditions.keySet()) {
            int value = 100 - (skinConditions.get(condition).intValue());

            // Create a SpannableString to hold the formatted text
            SpannableString spannableString = new SpannableString(condition + ": " + value + "%\n");

            // Set the color based on the value
            if (value >= 80) {
                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (value <= 50) {
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // Append the colored SpannableString to results
            results.append(spannableString.toString()); // Convert SpannableString to String
        }
        return results.toString();
    }

    private void displayAnalysisImage(String imageUrl) {
        ImageView analysisImageView = findViewById(R.id.analysisImage);

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(analysisImageView);

            // Rotate the ImageView itself if the image appears rotated
            analysisImageView.setRotation(270); // Adjust this angle if necessary
        } else {
            Log.d("Image Load Error", "Image URL is null");
        }
    }

    private void loadRecommendedProducts() {
        firestore.collection("skincare_products") // Replace with your actual Firestore collection path
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);

                            // Check if the product matches the user's skin type and skin condition
                            if (isProductRecommended(product)) {
                                productList.add(product);
                            }
                        }
                        recommendedProductsAdapter.notifyDataSetChanged(); // Notify the adapter of data change
                    } else {
                        Log.e("Firestore Error", "Error getting products: ", task.getException());
                    }
                });
    }

    // Method to check if the product is recommended based on skin type and skin condition
    private boolean isProductRecommended(Product product) {
        // Split the afterUses string to check if it includes conditions matching skin analysis
        String[] afterUsesArray = product.getAfterUse().split(",");
        List<String> afterUses = Arrays.asList(afterUsesArray);

        String[] functionsArray = product.getFunction().split(",");
        List<String> functions = Arrays.asList(functionsArray);

        // Check for skin type and condition
        boolean matchesAfterUse = afterUses.stream().anyMatch(afterUse -> {
            switch (skinType) {
                case "Sensitive":
                    return afterUse.contains("Redness Reducing") || afterUse.contains("Reduces Irritation");
                case "Oily":
                    return afterUse.contains("Good For Oily Skin") || afterUse.contains("Acne Trigger");
                case "Dry":
                    return afterUse.contains("Hydrating") || afterUse.contains("Anti-Aging");
                case "Combination":
                    return afterUse.contains("Redness Reducing") || afterUse.contains("Hydrating");
                case "Normal":
                    return true; // Normal skin can use most products
                default:
                    return false; // Default case if skin type is unknown
            }
        });
        Log.d("Skin Analysis", "Matches After Use: " + matchesAfterUse);

        // Check if the product's function matches the expected function for the skin type
        boolean matchesFunction = functions.stream().anyMatch(function -> {
            switch (skinType) {
                case "Sensitive":
                    return function.contains("Sensitive Skin Care");
                case "Oily":
                    return function.contains("Oily Skin Care");
                case "Dry":
                    return function.contains("Dry Skin Care");
                case "Combination":
                    return function.contains("Combination Skin Care");
                case "Normal":
                    return true; // Normal skin can use most products
                default:
                    return false; // Default case if skin type is unknown
            }
        });

        // Return true if both conditions match
        return matchesAfterUse && matchesFunction;
    }

}
