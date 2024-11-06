package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        // Load cover image using Glide
        String coverImageUri = content.getCoverImage(); // Assuming coverImageUri is the URL string
        if (coverImageUri != null && !coverImageUri.isEmpty()) {
            Glide.with(context)
                    .load(coverImageUri)  // Load the URL string with Glide
                    .placeholder(R.drawable.baseline_image_24) // Optional placeholder
                    .error(R.drawable.baseline_image_24)       // Optional error image
                    .into(holder.imageView);
        } else {
            // Set a default image if no cover image URL is available
            holder.imageView.setImageResource(R.drawable.baseline_image_24);
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

