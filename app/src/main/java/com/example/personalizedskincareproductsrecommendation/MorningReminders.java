package com.example.personalizedskincareproductsrecommendation;

public class MorningReminders extends Reminder {
    private String title;
    private String time;
    private String days;

    // No-argument constructor required by Firebase
    public MorningReminders() {
        // This constructor is intentionally empty. It is required by Firebase.
        super();
    }

    // Constructor with parameters
    public MorningReminders(String title, String time, String key, String days) {
        super(title, time, null, days); // Pass title and time to the superclass
        this.title = title;
        this.time = time;
        this.days = days;
    }

    // Getters and Setters
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

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }
}
