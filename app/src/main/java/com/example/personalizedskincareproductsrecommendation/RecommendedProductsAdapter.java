package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecommendedProductsAdapter extends RecyclerView.Adapter<RecommendedProductsAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public RecommendedProductsAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommended_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.nameTextView.setText(product.getName());
        holder.brandTextView.setText(product.getBrand());
        holder.typeTextView.setText(product.getType());
        Glide.with(context)
                .load(product.getImageUrl()) // Ensure your Product class has an imageUrl field if needed
                .into(holder.productImage);

        holder.productLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Product clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ProductDetails.class);
                intent.putExtra("productBrand", product.getBrand());
                intent.putExtra("productType", product.getType());
                intent.putExtra("productFunction", product.getFunction());
                intent.putExtra("productIngredients", product.getIngredients());
                intent.putExtra("imageUrl", product.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, brandTextView, typeTextView;
        ImageView productImage;
        LinearLayout productLayout;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            brandTextView = itemView.findViewById(R.id.product_brand);
            typeTextView = itemView.findViewById(R.id.product_type);
            productImage = itemView.findViewById(R.id.product_image);
            productLayout = itemView.findViewById(R.id.product_layout);
        }
    }

    public void updateProductList(List<Product> products) {
        this.productList.clear();
        this.productList.addAll(products);
        notifyDataSetChanged();
    }



}

