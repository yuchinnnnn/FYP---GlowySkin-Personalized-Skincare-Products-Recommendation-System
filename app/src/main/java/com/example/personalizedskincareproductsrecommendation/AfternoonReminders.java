package com.example.personalizedskincareproductsrecommendation;

public class AfternoonReminders extends Reminder {
    private String title;
    private String time;
    private String days;

    // No-argument constructor required by Firebase
    public AfternoonReminders() {
        // This constructor is intentionally empty. It is required by Firebase.
        super();
    }

    // Constructor with parameters
    public AfternoonReminders(String title, String time, String key, String days) {
        super(title, time, null, days); // Pass the title, time, and a null key to the superclass
        this.title = title;
        this.time = time;
        this.days = days;
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
}
