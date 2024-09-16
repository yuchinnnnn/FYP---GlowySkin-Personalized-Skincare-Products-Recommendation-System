package com.example.personalizedskincareproductsrecommendation;

public class Reminder {
    private String title;
    private String time;
    private String key;  // Add this field to store the reminder key
    private String days;

    // No-argument constructor for Firebase
    public Reminder() {
    }

    // Constructor with arguments (optional)
    public Reminder(String title, String time, String key, String days) {
        this.title = title;
        this.time = time;
        this.key = key;  // Initialize the key field
        this.days = days;
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
}

