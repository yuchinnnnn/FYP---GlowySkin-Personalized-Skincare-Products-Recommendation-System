package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminManageProducts extends AppCompatActivity {
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private ListView productListView;
    private ProductAdapter productAdapter;
    private ArrayList<Product> productList;
    private ArrayAdapter<String> adapter;
    private ImageView back;
    private TextView title;
    private AutoCompleteTextView searchView;
    private ImageButton add;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_products);

        userId = getIntent().getStringExtra(ARG_USER_ID);

        // Bind views to their XML counterparts
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        searchView = findViewById(R.id.hint_text);
        add = findViewById(R.id.add_button);

        // Set the layout manager for the RecyclerView
        db = FirebaseFirestore.getInstance();
        productListView = findViewById(R.id.product_list); // Update this to match your ListView ID
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList, userId);
        productListView.setAdapter(productAdapter);

        // Handle the back button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous activity
                finish();
            }
        });

        // Handle the add image button click (Add new product)
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to another activity to add new product
                Intent intent = new Intent(AdminManageProducts.this, AdminAddProducts.class);
                intent.putExtra(ARG_USER_ID, userId);
                startActivity(intent);
            }
        });
//
//        searchView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String searchTerm = s.toString();
//                searchProducts(searchTerm);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });


        // Fetch and display all products
        fetchProducts();
        fetchProductsForAutocomplete();

    }

    private void fetchProducts() {
        db.collection("skin_care_product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();  // Clear the list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (document.contains("image_url")) {
                                // Safely retrieve image_url and set a default if it's null or invalid
                                String imageUrl = document.getString("image_url");
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    product.setImageUrl(imageUrl);
                                } else {
                                    // Set a default image URL or a placeholder if image_url is not valid
                                    product.setImageUrl("default_image_url"); // Replace with your actual default image URL
                                    Log.e("AdminManageProducts", "image_url is not a valid String, setting default.");
                                }

                                // Now retrieve other fields since image_url exists
                                String id = document.getString("id");
                                String brand = document.getString("brand");
                                String name = document.getString("name");

                                Log.d(TAG, "Product: " + id + " " + brand + " " + name + " " + product.getImageUrl());

                                product.setBrand(brand);
                                product.setName(name);

                                // Add the product to the list
                                productList.add(product);
                            } else {
                                Log.d(TAG, "Product does not have an image_url.");
                            }
                        }

                            // Notify the adapter that the data has changed
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    // Method to search products
    // Method to search products
    private void searchProducts(String searchTerm) {
        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        // Update the adapter with the filtered list
        productAdapter.updateProductList((ArrayList<Product>) filteredProducts);
    }

    private void fetchProductsForAutocomplete() {
        db.collection("skin_care_product")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> productNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("name") && document.contains("brand")) {
                                String name = document.getString("name");
                                String brand = document.getString("brand");
                                productNames.add(name + " - " + brand);  // Combine name and brand into a single string
                            }
                        }
                        // Set the adapter for hintText with the product names
                        adapter = new ArrayAdapter<>(AdminManageProducts.this, android.R.layout.simple_dropdown_item_1line, productNames);
                        searchView.setAdapter(adapter);
                        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                String item = adapterView.getItemAtPosition(i).toString();
                                filterProductsBySelectedItem(item);
                            }
                        });
                    } else {
                        Log.w(TAG, "Error getting product names for autocomplete.", task.getException());
                    }
                });
    }

    private void filterProductsBySelectedItem(String selectedItem) {
        String[] parts = selectedItem.split(" - ");
        if (parts.length == 2) {
            String selectedName = parts[0];
            String selectedBrand = parts[1];

            ArrayList<Product> filteredList = new ArrayList<>();
            for (Product product : productList) {
                if (product.getName().equals(selectedName) && product.getBrand().equals(selectedBrand)) {
                    filteredList.add(product);
                }
            }

            // Update the adapter with the filtered list
            productAdapter.updateProductList(filteredList);
        }
    }


}
