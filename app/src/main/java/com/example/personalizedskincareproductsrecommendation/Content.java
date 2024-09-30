package com.example.personalizedskincareproductsrecommendation;

import java.util.List;

public class Content {
    private String title, uploadDateTime, userId, id;
    private List<String> imageUrls;

    // Required empty constructor for Firebase
    public Content() {}

    public Content(String title, String uploadDateTime, List<String> imageUrls) {
        this.title = title;
        this.uploadDateTime = uploadDateTime;
        this.imageUrls = imageUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploadDateTime() {
        return uploadDateTime;
    }

    public void setUploadDateTime(String date) {
        this.uploadDateTime = date;
    }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
