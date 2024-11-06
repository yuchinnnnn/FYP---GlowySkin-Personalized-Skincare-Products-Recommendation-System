package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductDetails extends AppCompatActivity {

    private ImageView back, productImage;
    private TextView name, afterUse, brand, type, ingredients, function;
    private RatingBar ratingBar;
    private EditText feedbackInput;
    private Button submitFeedbackButton;

    private DatabaseReference feedbackRef;
    private String userId, productId, productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize Firebase reference for Feedback table
        feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback");

        // Initialize Views
        back = findViewById(R.id.back);
        productImage = findViewById(R.id.productImage);
        brand = findViewById(R.id.brand);
        name = findViewById(R.id.name);
        afterUse = findViewById(R.id.afterUse);
        type = findViewById(R.id.type);
        function = findViewById(R.id.function);
        ingredients = findViewById(R.id.ingredients);

        // Initialize feedback components
//        ratingBar = (RatingBar) findViewById(R.id.rating);
//        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                Log.d("ProductDetails", "Rating changed: " + rating);
//                // Optional: Handle rating change here if needed
//            }
//        });
//
//        feedbackInput = findViewById(R.id.feedbackInput);
//        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        // Set up back button functionality
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Assume product details are passed via Intent
        Intent intent = getIntent();
        if (intent != null) {
            productName = intent.getStringExtra("productName");
            String productBrand = intent.getStringExtra("productBrand");
            String productType = intent.getStringExtra("productType");
            String productIngredients = intent.getStringExtra("productIngredients");
            String productFunction = intent.getStringExtra("productFunction");
            String productUsage = intent.getStringExtra("productAfterUse");
            String imageUrl = intent.getStringExtra("image_url");

            // Set text data
            brand.setText(productBrand);
            name.setText(productName);
            afterUse.setText(productUsage);
            type.setText(productType);
            ingredients.setText(productIngredients);
            function.setText(productFunction);

            // Load image using Glide or any image loading library
            Glide.with(this).load(imageUrl).into(productImage);
        }

        // Set up feedback submission
//        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                submitFeedback();
//            }
//        });
    }

//    private void submitFeedback() {
//        float rating = ratingBar.getRating();
//        String feedbackText = feedbackInput.getText().toString().trim();
//
//        if (rating == 0) {
//            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (feedbackText.isEmpty()) {
//            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Get current date and time
//        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        // Prepare feedback data
//        Map<String, Object> feedbackData = new HashMap<>();
//        feedbackData.put("productName", productName);
//        feedbackData.put("userId", userId);
//        feedbackData.put("rating", rating);
//        feedbackData.put("feedbackText", feedbackText);
//        feedbackData.put("dateTime", dateTime);
//
//        // Save feedback in Firebase under a unique ID
//        feedbackRef.push().setValue(feedbackData)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(ProductDetails.this, "Feedback submitted!", Toast.LENGTH_SHORT).show();
//                        feedbackInput.setText("");
//                        ratingBar.setRating(0);
//                    } else {
//                        Toast.makeText(ProductDetails.this, "Failed to submit feedback. Try again!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


}
