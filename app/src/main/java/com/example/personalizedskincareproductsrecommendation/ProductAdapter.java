package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

    public ProductAdapter(Context context, ArrayList<Product> productList) {
        super(context, 0, productList);
        this.isAdmin = isAdmin;  // Store the admin status
    }

    // ViewHolder pattern for caching views
    private static class ViewHolder {
        TextView brandTextView, nameTextView, typeTextView;
        ImageView productImageView, editButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.products_list, parent, false);

            viewHolder.brandTextView = convertView.findViewById(R.id.product_brand);
            viewHolder.nameTextView = convertView.findViewById(R.id.product_name);
            viewHolder.typeTextView = convertView.findViewById(R.id.product_type);
            viewHolder.productImageView = convertView.findViewById(R.id.product_image);
            viewHolder.editButton = convertView.findViewById(R.id.edit_button);  // Admin-specific view
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Bind data
        viewHolder.brandTextView.setText(product.getBrand());
        viewHolder.nameTextView.setText(product.getName());
        viewHolder.typeTextView.setText(product.getType());
        Glide.with(getContext()).load(product.getImageUrl()).into(viewHolder.productImageView);

        // Show or hide the edit button based on admin status
        if (isAdmin) {
            viewHolder.editButton.setVisibility(View.VISIBLE);
            viewHolder.editButton.setOnClickListener(v -> {
                // Handle edit action
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
