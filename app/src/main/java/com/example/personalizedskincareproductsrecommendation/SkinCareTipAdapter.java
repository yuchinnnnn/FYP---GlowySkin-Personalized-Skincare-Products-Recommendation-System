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
        holder.title.setText(tip.getTitle());
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TipDetails.class);
            intent.putExtra(tip.getId(), tip.getTitle());
            context.startActivity(intent);
            Toast.makeText(context, "View " + tip.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Alternatively, load the first image directly into an ImageView if only showing one
        if (!tip.getImages().isEmpty()) {
            Glide.with(context)
                    .load(tip.getImages().get(0)) // Load the first image
                    .into(holder.imageView);
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
            title = itemView.findViewById(R.id.tipTitle);
            imageView = itemView.findViewById(R.id.tipImage);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}

