package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        holder.functionTextView.setText(product.getFunction());
        Glide.with(context)
                .load(product.getImageUrl()) // Ensure your Product class has an imageUrl field if needed
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, brandTextView, functionTextView;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.productName);
            brandTextView = itemView.findViewById(R.id.product_brand);
            functionTextView = itemView.findViewById(R.id.product_function);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }
}

