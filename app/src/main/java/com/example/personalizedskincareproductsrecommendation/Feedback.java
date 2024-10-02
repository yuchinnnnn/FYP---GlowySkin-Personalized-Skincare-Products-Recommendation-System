package com.example.personalizedskincareproductsrecommendation;

public class Feedback {
    private String username; // Name of the user who provided feedback
    private String feedbackText; // The feedback text
    private int rating; // Rating given by the user
    private String date; // Date of the feedback

    public Feedback(String username, String feedbackText, int rating, String date) {
        this.username = username;
        this.feedbackText = feedbackText;
        this.rating = rating;
        this.date = date;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public int getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }
}
