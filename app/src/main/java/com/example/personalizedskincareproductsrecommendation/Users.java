package com.example.personalizedskincareproductsrecommendation;

public class Users {
    private String userId, username, email, status, skinQuizResults;
    private int age;
    private boolean isActive; // true for active, false for deactivated
    private boolean isPendingApproval;


    public Users() {
        // Default constructor required for Firebase
    }

    public Users(String userId, String username, String email, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSkinQuizResults() {
        return skinQuizResults;
    }

    public void setSkinQuizResults(String skinQuizResults) {
        this.skinQuizResults = skinQuizResults;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isPendingApproval() {
        return isPendingApproval;
    }

    public void setPendingApproval(boolean pendingApproval) {
        isPendingApproval = pendingApproval;
    }
}

