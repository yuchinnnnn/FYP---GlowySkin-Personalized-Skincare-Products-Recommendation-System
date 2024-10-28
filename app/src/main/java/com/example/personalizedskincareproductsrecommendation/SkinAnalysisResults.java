package com.example.personalizedskincareproductsrecommendation;

import java.time.LocalDateTime;
import java.util.Map;

public class SkinAnalysisResults {
    private String userId;
    private String keyId;
    private LocalDateTime dateTime;
    private float[] skinConditionPercentages;
    private String skinType;
    private String imageUrl;
    private Map<String, Float> skinConditions;

    public SkinAnalysisResults(String userId, String keyId, LocalDateTime dateTime, String imageUrl, String skinType, Map<String, Float> skinConditions) {
        this.userId = userId;
        this.keyId = keyId;
        this.dateTime = dateTime;
//        this.skinConditionPercentages = skinConditionPercentages;
        this.imageUrl = imageUrl;
        this.skinType = skinType;
        this.skinConditions = skinConditions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public float[] getSkinConditionPercentages() {
        return skinConditionPercentages;
    }

    public void setSkinConditionPercentages(float[] skinConditionPercentages) {
        this.skinConditionPercentages = skinConditionPercentages;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Map<String, Float> getSkinConditions() {
        return skinConditions;
    }
}
