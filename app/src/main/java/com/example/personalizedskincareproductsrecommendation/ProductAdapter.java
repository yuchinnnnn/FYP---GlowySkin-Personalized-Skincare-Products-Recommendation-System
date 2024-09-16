package com.example.personalizedskincareproductsrecommendation;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private ArrayList<Product> productList;

    public ProductAdapter(Context context, ArrayList<Product> productList) {
        super(context, 0, productList);
        this.context = context;
        this.productList = productList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Product product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.products_list, parent, false);
        }

        TextView brandTextView = convertView.findViewById(R.id.product_brand);
        TextView nameTextView = convertView.findViewById(R.id.product_name);
        TextView typeTextView = convertView.findViewById(R.id.product_type);
        ImageView productImageView = convertView.findViewById(R.id.product_image);

        brandTextView.setText(product.getBrand());
        nameTextView.setText(product.getName());
        typeTextView.setText(product.getType());
        // Load image using a library like Glide or Picasso
        Glide.with(getContext()).load(product.getImageUrl()).into(productImageView);

        return convertView;
    }

    public void updateProductList(ArrayList<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

}

