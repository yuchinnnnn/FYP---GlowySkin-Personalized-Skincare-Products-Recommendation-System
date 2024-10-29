package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ProductDetails extends AppCompatActivity {

    private ImageView back, productImage;
    private TextView brand, type, ingredients, function;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize Views
        back = findViewById(R.id.back);
        productImage = findViewById(R.id.productImage);
        brand = findViewById(R.id.brand);
        type = findViewById(R.id.type);
        function = findViewById(R.id.function);
        ingredients = findViewById(R.id.ingredients);

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
            String productBrand = intent.getStringExtra("productBrand");
            String productType = intent.getStringExtra("productType");
            String productIngredients = intent.getStringExtra("productIngredients");
            String productFunction = intent.getStringExtra("productFunction");
//            String skinType = intent.getStringExtra("skinType");
            String imageUrl = intent.getStringExtra("imageUrl");

            // Set text data
            brand.setText(productBrand);
            type.setText(productType);
            ingredients.setText(productIngredients);
            function.setText(productFunction);

            // Load image using Glide or any image loading library
            Glide.with(this).load(imageUrl).into(productImage);
        }
    }
}