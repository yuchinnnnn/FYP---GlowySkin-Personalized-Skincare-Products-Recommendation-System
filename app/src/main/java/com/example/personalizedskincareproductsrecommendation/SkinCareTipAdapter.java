package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SkinCareTipAdapter extends RecyclerView.Adapter<SkinCareTipAdapter.ViewHolder> {
    private Context context;
    private List<SkinCareTip> skinCareTipsList;

    public SkinCareTipAdapter(Context context, List<SkinCareTip> skinCareTipsList) {
        this.context = context;
        this.skinCareTipsList = skinCareTipsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.skincare_tips_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SkinCareTip tip = skinCareTipsList.get(position);
//        holder.title.setText(tip.getTitle());
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TipDetails.class);
            intent.putExtra("tipId", tip.getId());  // Pass the tipId with the key "tipId"
            context.startActivity(intent);
        });

        String coverImage = tip.getCoverImage();
        List<String> images = tip.getImages();

        if (coverImage != null && !coverImage.isEmpty()) {
            // Construct the full Firebase Storage URL for the cover image
            Glide.with(context)
                    .load(coverImage)  // Load the URL string with Glide
                    .placeholder(R.drawable.baseline_image_24) // Optional placeholder
                    .error(R.drawable.baseline_image_24)       // Optional error image
                    .into(holder.imageView);
        }
        else if(coverImage == null && images !=null) {
            Glide.with(context)
                    .load(tip.getImages().get(0)) // Load the first image
                    .into(holder.imageView);
        }
        else{
            Toast.makeText(context, "Cover image not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return skinCareTipsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CardView cardView;
        ImageView imageView; // Or a ViewPager

        ViewHolder(View itemView) {
            super(itemView);
//            title = itemView.findViewById(R.id.tipTitle);
            imageView = itemView.findViewById(R.id.coverImage);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}


