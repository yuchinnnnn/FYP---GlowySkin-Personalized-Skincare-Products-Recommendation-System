package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SuggestedProductAdapter extends RecyclerView.Adapter<SuggestedProductAdapter.SuggestedProductViewHolder> {

    private Context context;
    private ArrayList<Product> productList;

    public SuggestedProductAdapter(Context context, ArrayList<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public SuggestedProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.suggested_item_listview, parent, false);
        return new SuggestedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SuggestedProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set product image, brand name, etc.
        holder.brandName.setText(product.getBrand());
        // You can set product image using Picasso or Glide here
        // Picasso.get().load(product.getImageUrl()).into(holder.productImage);

        holder.viewButton.setOnClickListener(v -> {
            // Handle view button click, open product details, etc.
            Toast.makeText(context, "View " + product.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class SuggestedProductViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView brandName;
        Button viewButton;

        public SuggestedProductViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.skincare_image);
            brandName = itemView.findViewById(R.id.skincare_brand);
            viewButton = itemView.findViewById(R.id.view_button);
        }
    }
}
