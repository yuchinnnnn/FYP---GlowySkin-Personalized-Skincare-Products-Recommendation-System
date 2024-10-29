package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinAnalysisResult extends AppCompatActivity {

    private TextView skinTypeResult, skinConditionResult;
    private RecyclerView recommendedProductsRecyclerView;
    private ImageView back, info, filter;
    private String userId, keyId, skinType, targetCondition, lowestCondition;
    private FirebaseFirestore firestore;
    private RecommendedProductsAdapter recommendedProductsAdapter;
    private ArrayList<Product> productList;
    private ArrayList<Product> originalProductList;
    private HashMap<String, List<String>> skinTypeMapping;
    private HashMap<String, List<String>> skinConditionMapping;
    HashMap<String, Object> someMap = new HashMap<>();


    private void setupSkinTypeMapping() {
        skinTypeMapping = new HashMap<>();
        skinTypeMapping.put("Sensitive", Arrays.asList("Redness Reducing", "Reduces Irritation"));
        skinTypeMapping.put("Oily", Arrays.asList("Good For Oily Skin"));
        skinTypeMapping.put("Dry", Arrays.asList("Hydrating"));
        skinTypeMapping.put("Combination", Arrays.asList("Redness Reducing", "Hydrating"));
        skinTypeMapping.put("Normal", Arrays.asList("General Care"));
    }

    private void setupSkinConditionMapping() {
        skinConditionMapping = new HashMap<>();
        skinConditionMapping.put("acne", Arrays.asList("Acne Control", "Acne Fighting"));
        skinConditionMapping.put("redness", Arrays.asList("Redness Reducing", "Soothing","Reduces Irritation"));
        skinConditionMapping.put("wrinkles", Arrays.asList("Anti-Aging"));
        skinConditionMapping.put("darkSpots", Arrays.asList("Brightening", "Dark Spot"));
        skinConditionMapping.put("pores", Arrays.asList("Reduces Large Pores", "Minimizes Pores"));
        skinConditionMapping.put("darkCircle", Arrays.asList("Brightening"));
    }

    private boolean isProductRecommended(Product product, String targetCondition) {
        List<String> afterUses = Arrays.asList(product.getAfterUse().split(","));
        List<String> functions = Arrays.asList(product.getFunction().split(","));

        // Check if the product matches the skin type
        boolean matchesSkinType = false;
        List<String> recommendedFunctions = skinTypeMapping.get(skinType);

        if (recommendedFunctions != null) {
            for (String afterUse : afterUses) {
                // Check if the afterUse matches the user's skin type
                if (recommendedFunctions.stream().anyMatch(afterUse::contains)) {
                    matchesSkinType = true;
                    break;
                }
            }
        }

        boolean matchesCondition = false;
        List<String> recommendedConditionFunctions = skinConditionMapping.get(targetCondition);
        Log.d(TAG, "Target condition: " + targetCondition);
        if (recommendedConditionFunctions != null) {
            for (String afterUse : afterUses) {
                if (recommendedConditionFunctions.stream().anyMatch(afterUse::contains)) {
                    matchesCondition = true;
                    Log.d(TAG, "Matched condition: " + afterUse);
                    break;
                }
            }
        }

        return matchesSkinType && matchesCondition;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_analysis_result);

        initializeViews();
        setupListeners();
        setupSkinTypeMapping();
        setupSkinConditionMapping();

        // Retrieve userId and keyId from Intent extras
        userId = getIntent().getStringExtra("ARG_USER_ID");
        keyId = getIntent().getStringExtra("ARG_ANALYSIS_ID");
        if (userId == null || keyId == null) {
            Log.e("Intent Error", "userId or keyId is null.");
            return;
        }

        firestore = FirebaseFirestore.getInstance();

        // save cache
        FirebaseFirestore.getInstance().setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
        );


        filter = findViewById(R.id.filter);
        filter.setOnClickListener(v -> {
            showFilterOptions();
        });

        // Load data
        loadSkinAnalysisData();
    }

    private void showFilterOptions() {
        String[] productTypes = {"Face Cleanser", "Toner", "Serum", "General Moisturizer", "Facial Treatment",
                "Sunscreen", "Exfoliator", "Essence", "Sheet Mask", "Overnight Mask"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Product Type")
                .setItems(productTypes, (dialog, which) -> {
                    String selectedType = productTypes[which];
                    filterProductsByType(selectedType);
                });

        builder.create().show();
    }

    private void initializeViews() {
        skinTypeResult = findViewById(R.id.skinTypeResult);
        skinConditionResult = findViewById(R.id.skinConditionResult);
        recommendedProductsRecyclerView = findViewById(R.id.recommendedProductsRecyclerView);
        recommendedProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        originalProductList = new ArrayList<>(); // Initialize the original list
        recommendedProductsAdapter = new RecommendedProductsAdapter(this, productList);
        recommendedProductsRecyclerView.setAdapter(recommendedProductsAdapter);

        back = findViewById(R.id.back);
        info = findViewById(R.id.infoIcon);
        filter = findViewById(R.id.filter);
    }

    private void setupListeners() {
        back.setOnClickListener(v -> finish());
        info.setOnClickListener(v -> showInfoDialog());
    }

    private void loadSkinAnalysisData() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("SkinAnalysis").child(userId).child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleAnalysisSnapshot(snapshot);
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

    private void handleAnalysisSnapshot(DataSnapshot snapshot) {
        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
        skinType = snapshot.child("skinType").getValue(String.class);
        HashMap<String, Float> skinCondition = new HashMap<>();
        float lowestValue = Float.MAX_VALUE;
        String lowestCondition = null;  // Initialize as null to store the condition with the lowest value

        for (DataSnapshot conditionSnapshot : snapshot.child("skinCondition").getChildren()) {
            String condition = conditionSnapshot.getKey();
            float originalValue = conditionSnapshot.getValue(Float.class);
            float adjustedValue = 100 - originalValue; // Calculate the adjusted value

            skinCondition.put(condition, adjustedValue);

            // Compare the adjusted value to find the lowest condition
            if (adjustedValue < lowestValue) {
                lowestValue = adjustedValue;
                lowestCondition = condition;  // Update lowest condition to the current condition
            }
            Log.d("SkinAnalysisResult", "Condition: " + condition + ", Original Value: " + originalValue + ", Adjusted Value: " + adjustedValue);
            Log.d("SkinAnalysisResult", "Lowest Condition: " + lowestCondition + ", Lowest Value: " + lowestValue);
        }

        displayAnalysisImage(imageUrl);
        skinTypeResult.setText(skinType);
        skinConditionResult.setText(formatSkinConditionResults(skinCondition));

        // Load recommended products based on the lowest condition
        if (lowestCondition != null) {
            loadRecommendedProducts(lowestCondition);
        } else {
            Log.w("Lowest Condition", "No valid condition found for recommendation.");
        }
    }


    private String formatSkinConditionResults(HashMap<String, Float> skinConditions) {
        StringBuilder results = new StringBuilder();
        for (String condition : skinConditions.keySet()) {
            int value = skinConditions.get(condition).intValue();
            String resultLine = condition + ": " + value + "%\n";

            // Apply color based on severity
            int color = value >= 80 ? Color.GREEN : value <= 50 ? Color.RED : Color.BLACK;
            results.append(getColoredText(resultLine, color));
        }
        return results.toString();
    }

    private SpannableString getColoredText(String text, int color) {
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private void displayAnalysisImage(String imageUrl) {
        ImageView analysisImageView = findViewById(R.id.analysisImage);
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(analysisImageView);
            analysisImageView.setRotation(270);  // Adjust rotation if necessary
        } else {
            Log.d("Image Load Error", "Image URL is null");
        }
    }

    private void loadRecommendedProducts(String targetCondition) {
        firestore.collection("skincare_products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                productList.clear();
                originalProductList.clear(); // Clear original list before adding new items

                HashMap<String, List<Product>> productsByType = new HashMap<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Product product = document.toObject(Product.class);
                    if (isProductRecommended(product, targetCondition)) {
                        String productType = product.getType();

                        if (!productsByType.containsKey(productType)) {
                            productsByType.put(productType, new ArrayList<>());
                        }

                        List<Product> productListForType = productsByType.get(productType);
                        if (productListForType.size() < 5) {
                            productListForType.add(product);
                        }
                    }
                }

                for (List<Product> products : productsByType.values()) {
                    productList.addAll(products);
                    originalProductList.addAll(products); // Add to the original list as well
                }

                recommendedProductsAdapter.notifyDataSetChanged();
                Log.d("Product Load", "Total products loaded: " + productList.size());
            } else {
                Log.e("Firestore Error", "Error getting products: ", task.getException());
            }
        });
    }

    private void filterProductsByType(String selectedType) {
        ArrayList<Product> filteredRecommendedList = new ArrayList<>();

        for (Product product : originalProductList) { // Use original list here
            if (product.getType() != null && product.getType().equalsIgnoreCase(selectedType)) {
                filteredRecommendedList.add(product);
            }
        }
        Log.d("Filtered Products", "Total filtered products: " + filteredRecommendedList.size());

        recommendedProductsAdapter.updateProductList(filteredRecommendedList);

        if (filteredRecommendedList.isEmpty()) {
            showNoProductsFoundDialog(selectedType);
        }
    }

    private void showInfoDialog() {
        SweetAlertDialog dialog = new SweetAlertDialog(SkinAnalysisResult.this, SweetAlertDialog.NORMAL_TYPE);
        dialog.setTitleText("Information");
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

        dialog.setContentText(message);
        dialog.show();
    }

    // Helper method to show a dialog if no products are found
    private void showNoProductsFoundDialog(String selectedType) {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("No Products Found");
        dialog.setContentText("No products found for the selected type: " + selectedType);
        dialog.show();
    }



}
