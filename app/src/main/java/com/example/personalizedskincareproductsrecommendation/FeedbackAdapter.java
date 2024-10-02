package com.example.personalizedskincareproductsrecommendation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList;

    public FeedbackAdapter(List<Feedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        // Bind data to views
        holder.username.setText(feedback.getUsername());
        holder.date.setText(feedback.getDate());  // Assuming the date is formatted in a readable way
        holder.feedback.setText(feedback.getFeedbackText());
        holder.rating.setRating(feedback.getRating());

        // Handle reply icon click if needed
        holder.reply.setOnClickListener(v -> {
            // Handle the reply action here, e.g., open a dialog to reply to feedback
            Toast.makeText(v.getContext(), "Reply to " + feedback.getUsername(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        TextView username, date, feedback;
        RatingBar rating;
        ImageView reply;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the views
            username = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            feedback = itemView.findViewById(R.id.feedback);
            rating = itemView.findViewById(R.id.rating);
            reply = itemView.findViewById(R.id.reply);
        }
    }
}
