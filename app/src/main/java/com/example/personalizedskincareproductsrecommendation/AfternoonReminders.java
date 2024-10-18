package com.example.personalizedskincareproductsrecommendation;

public class AfternoonReminders extends Reminder {
    private String title;
    private String time;
    private String key;
    private String days;
    private String status;
    private Boolean isRemoved, isPassed;

    // No-argument constructor required by Firebase
    public AfternoonReminders() {
        // This constructor is intentionally empty. It is required by Firebase.
        super();
    }

    // Constructor with parameters
    public AfternoonReminders(String title, String time, String key, String days, Boolean isRemoved, Boolean isPassed, String status) {
        this.title = title;
        this.time = time;
        this.key = key;  // Initialize the key field
        this.days = days;
        this.isRemoved = isRemoved;
        this.isPassed = isPassed;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getters and Setters
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Boolean getIsRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(Boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public Boolean getIsPassed() {
        return isPassed;
    }

    public void setIsPassed(Boolean isPassed) {
        this.isPassed = isPassed;
    }

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
}
