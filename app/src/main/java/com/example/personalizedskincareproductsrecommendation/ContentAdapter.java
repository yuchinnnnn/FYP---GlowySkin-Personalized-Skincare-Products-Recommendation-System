package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    private Context context;
    private List<Content> contentList;
    public static final String ARG_USER_ID = "userId";
    public static final String ARG_CONTENT_ID = "contentId";

    public ContentAdapter(Context context, List<Content> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_list, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        Content content = contentList.get(position);

        // Set the title and date
        holder.title.setText(content.getTitle());
        holder.date.setText(content.getUploadDateTime());

        // Load the first image from the list, if available
        List<String> imageUrls = content.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Glide.with(context)
                    .load(imageUrls.get(0))  // Load the first image in the list
                    .into(holder.imageView);
        } else {
            // Set a default image if no images are available
            holder.imageView.setImageResource(R.drawable.baseline_image_24);  // Replace with your placeholder image
        }

        // Set Edit button click listener
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEditContent.class);
            intent.putExtra(AdminEditContent.ARG_USER_ID, content.getUserId());
            intent.putExtra(AdminEditContent.ARG_CONTENT_ID, content.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, editButton;
        ImageView imageView;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            editButton = itemView.findViewById(R.id.edit_button);
            imageView = itemView.findViewById(R.id.content_image);
        }
    }
}

