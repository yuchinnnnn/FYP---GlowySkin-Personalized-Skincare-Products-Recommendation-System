package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AdminManageProducts extends AppCompatActivity {
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ImageView back;
    private TextView title;
    private AutoCompleteTextView searchView;
    private ImageButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_products);

        // Bind views to their XML counterparts
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        back = findViewById(R.id.back);
        title = findViewById(R.id.title);
        searchView = findViewById(R.id.hint_text);
        add = findViewById(R.id.add_button);

        // Set the layout manager for the RecyclerView
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the product list and adapter
        productList = new ArrayList<>();
//        productAdapter = new ProductAdapter(productList);
//        productsRecyclerView.setAdapter(productAdapter);

        // Handle the back button click
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous activity
                Intent intent = new Intent(AdminManageProducts.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        // Handle the add image button click (Add new product)
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to another activity to add new product
                Intent intent = new Intent(AdminManageProducts.this, AdminAddProducts.class);
                startActivity(intent);
            }
        });

        // Handle the search input
        searchView.setOnKeyListener((v, keyCode, event) -> {
            // Call search method when user types something
            String searchTerm = searchView.getText().toString();
            searchProducts(searchTerm);
            return false;
        });

        // Fetch and display all products
        fetchProducts();
    }

    private void fetchProducts() {
        // Sample data for demonstration purposes
//        productList.add(new Product("Product 1", "Description 1", 10.99));
//        productList.add(new Product("Product 2", "Description 2", 15.49));
//        productList.add(new Product("Product 3", "Description 3", 8.75));

        // Notify adapter about data changes
        productAdapter.notifyDataSetChanged();
    }

    // Method to search products
    private void searchProducts(String searchTerm) {
        // Filter the product list based on search term (you can fetch from the database as well)
        List<Product> filteredProducts = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        // Update the adapter with the filtered list
        productAdapter.updateProductList((ArrayList<Product>) filteredProducts);
    }
}
