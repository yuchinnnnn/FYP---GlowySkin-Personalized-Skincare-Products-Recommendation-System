package com.example.personalizedskincareproductsrecommendation;

public class SkinAnalysisEntry {
    private String keyId;
    private String imageUrl;
    private String skinType;
    private String uploadedDateTime;
    private SkinCondition skinCondition;  // Use the new SkinCondition class
    private String userId;

    // Constructor
    public SkinAnalysisEntry(String keyId, String uploadedDateTime, String userId, String imageUrl, String skinType, SkinCondition skinCondition) {
        this.keyId = keyId;
        this.uploadedDateTime = uploadedDateTime;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.skinType = skinType;
        this.skinCondition = skinCondition;  // Assign the SkinCondition instance
    }

    // Getters and Setters
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUploadedDateTime() {
        return uploadedDateTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public SkinCondition getSkinCondition() {
        return skinCondition;  // Get the SkinCondition object
    }

    public void setSkinCondition(SkinCondition skinCondition) {
        this.skinCondition = skinCondition;  // Set the SkinCondition object
    }
}
