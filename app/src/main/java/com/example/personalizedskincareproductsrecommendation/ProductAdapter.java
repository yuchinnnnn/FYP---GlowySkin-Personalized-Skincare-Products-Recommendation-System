package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {

    private boolean isAdmin;
    private FirebaseUser currentUser;
    private String userId;

    public ProductAdapter(Context context, ArrayList<Product> productList, String userId) {
        super(context, 0, productList);
        this.userId = userId;

        checkUser(userId);
    }

    private void checkUser(String userId) {
        // Reference to Admin table
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

        // Check if the user exists in the Admin table
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isAdmin = true; // User ID exists in Admin table
                    Log.d("UserCheck", "User is an Admin");
                } else {
                    isAdmin = false; // User ID does not exist in Admin table
                    Log.d("UserCheck", "User is not an Admin");

                    // Optionally check in Users table to ensure the user is a regular user
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            if (!userSnapshot.exists()) {
                                Log.d("UserCheck", "User does not exist in Users table");
                                Toast.makeText(getContext(), "User not found.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w("UserCheck", "Failed to read from Users table", error.toException());
                        }
                    });
                }
                // Notify the adapter that the user type has been determined, if needed
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("UserCheck", "Failed to read from Admin table", error.toException());
            }
        });
    }

    // ViewHolder pattern for caching views
    private static class ViewHolder {
        TextView brandTextView, nameTextView, typeTextView;
        ImageView productImageView, editButton;
        LinearLayout productLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.products_list, parent, false);

            // Initialize views
            viewHolder.brandTextView = convertView.findViewById(R.id.product_brand);
            viewHolder.nameTextView = convertView.findViewById(R.id.product_name);
            viewHolder.typeTextView = convertView.findViewById(R.id.product_type);
            viewHolder.productImageView = convertView.findViewById(R.id.product_image);
            viewHolder.productLayout = convertView.findViewById(R.id.product_layout);
            viewHolder.editButton = convertView.findViewById(R.id.edit_button);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Bind data to the views
        viewHolder.brandTextView.setText(product.getBrand());
        viewHolder.nameTextView.setText(product.getName());
        viewHolder.typeTextView.setText(product.getType());
        Glide.with(getContext()).load(product.getImageUrl()).into(viewHolder.productImageView);

        // Set up product click event
        viewHolder.productLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ProductDetails.class);
            intent.putExtra("productBrand", product.getBrand());
            intent.putExtra("productType", product.getType());
            intent.putExtra("productFunction", product.getFunction());
            intent.putExtra("productName", product.getName());
            intent.putExtra("productAfterUse", product.getAfterUse());
            intent.putExtra("productIngredients", product.getIngredients());
            intent.putExtra("image_url", product.getImageUrl());
            getContext().startActivity(intent);
        });

        // Show edit button if user is an admin
        if (isAdmin) {
            viewHolder.editButton.setVisibility(View.VISIBLE);
            viewHolder.editButton.setOnClickListener(v -> {
                // Handle edit button click for admin
                Intent editIntent = new Intent(getContext(), AdminEditProducts.class);
                editIntent.putExtra("productId", product.getId());
                editIntent.putExtra("productBrand", product.getBrand());
                editIntent.putExtra("productType", product.getType());
                editIntent.putExtra("productFunction", product.getFunction());
                editIntent.putExtra("productName", product.getName());
                editIntent.putExtra("productAfterUse", product.getAfterUse());
                editIntent.putExtra("productIngredients", product.getIngredients());
                editIntent.putExtra("image_url", product.getImageUrl());
                getContext().startActivity(editIntent);
            });
        } else {
            viewHolder.editButton.setVisibility(View.GONE);
        }

        return convertView;
    }

    // Method to update the product list
    public void updateProductList(ArrayList<Product> newList) {
        clear();
        addAll(newList);
        notifyDataSetChanged();
    }
}
