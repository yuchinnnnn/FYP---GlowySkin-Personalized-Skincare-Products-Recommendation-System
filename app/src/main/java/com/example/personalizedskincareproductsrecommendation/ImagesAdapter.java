package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.personalizedskincareproductsrecommendation.R;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls;
    private OnImageDeleteListener onImageDeleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(String imageUrl);
    }

    public ImagesAdapter(Context context, List<String> imageUrls, OnImageDeleteListener listener) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.onImageDeleteListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // Use Glide to load the image
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.baseline_image_24) // Optional placeholder
                .into(holder.imageView);

        holder.removeImageButton.setOnClickListener(v -> {
            onImageDeleteListener.onImageDelete(imageUrl); // Notify listener to delete the image
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView removeImageButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view);
            removeImageButton = itemView.findViewById(R.id.remove_image_button);
        }
    }
}

