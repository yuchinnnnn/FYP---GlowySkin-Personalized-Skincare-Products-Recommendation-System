package com.example.personalizedskincareproductsrecommendation;

public class Reminder {
    private String title;
    private String time;
    private String key;
    private String days;
    private String status;
    private Boolean isRemoved, isPassed;

    // No-argument constructor for Firebase
    public Reminder() {
    }

    // Constructor with arguments (optional)
    public Reminder(String title, String time, String key, String days, Boolean isRemoved, Boolean isPassed, String status) {
        this.title = title;
        this.time = time;
        this.key = key;  // Initialize the key field
        this.days = days;
        this.isRemoved = isRemoved;
        this.isPassed = isPassed;
        this.status = status;
    }

    // Getter and setter methods for each field
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKey() {
        return key;  // Add this getter method
    }

    public void setKey(String key) {
        this.key = key;  // Add this setter method
    }

    public String getDays(){return days;}
    public void setDays(String day){this.days = days;}

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

