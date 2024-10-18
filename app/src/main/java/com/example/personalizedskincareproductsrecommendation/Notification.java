package com.example.personalizedskincareproductsrecommendation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Notification extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Reminder> remindersList;
    private ImageView back;
    private FirebaseAuth mAuth;
    private String userId;
    private static final String ARG_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerView and list
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        remindersList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(remindersList, ""); // Initialize with an empty username
        recyclerView.setAdapter(notificationAdapter);

        // Add swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop functionality
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Reminder reminder = remindersList.get(position); // Get the reminder being swiped
                String reminderKey = reminder.getKey();

                // Update status to inactive
                DatabaseReference reminderRef = FirebaseDatabase.getInstance().getReference("Reminders")
                        .child(userId)
                        .child(reminderKey);
                reminderRef.child("status").setValue("inactive")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                remindersList.remove(position);  // Remove from the list locally
                                notificationAdapter.notifyItemRemoved(position);  // Notify the adapter
                                Toast.makeText(Notification.this, "Reminder marked as inactive", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Notification.this, "Failed to update reminder", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        userId = getIntent().getStringExtra(ARG_USER_ID);
        if (userId == null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch data from Firebase
        fetchUserDataAndReminders();
    }

    private void fetchUserDataAndReminders() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                if (username != null) {
                    notificationAdapter.updateUsername(username);
                    fetchReminders(); // Fetch reminders
                } else {
                    Toast.makeText(Notification.this, "Username not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Notification.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReminders() {
        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference("Reminders").child(userId);

        remindersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                remindersList.clear(); // Clear the list before adding new data

                for (DataSnapshot reminderSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve fields from each reminder snapshot
                    String title = reminderSnapshot.child("title").getValue(String.class);
                    String time = reminderSnapshot.child("time").getValue(String.class);
                    String key = reminderSnapshot.getKey();
                    String days = reminderSnapshot.child("days").getValue(String.class);
                    boolean isPassed = reminderSnapshot.child("isPassed").getValue(Boolean.class);
                    boolean isRemoved = reminderSnapshot.child("isRemoved").getValue(Boolean.class);
                    String status = reminderSnapshot.child("status").getValue(String.class);

                    // Create a Reminder object
                    Reminder reminder = new Reminder(title, time, key, days, isRemoved, isPassed, status);
                    remindersList.add(reminder); // Add to the list

                    // Schedule notifications for the reminder
                    scheduleNotification(reminder, notificationAdapter.getUsername(), key);
                }

                // Notify the adapter about data changes
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching reminders: " + databaseError.getMessage());
            }
        });
    }


    private void scheduleNotification(Reminder reminder, String username, String reminderKey) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            // Parse the reminder time
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTime(sdf.parse(reminder.getTime()));

            // Get the current time and day
            Calendar now = Calendar.getInstance();

            String daysString = reminder.getDays();
            if (daysString == null || daysString.isEmpty()) {
                Log.e("Notification", "Days are null or empty for reminder: " + reminder.getTitle());
                return; // Or handle accordingly
            }
            String[] daysOfWeek = daysString.split(",");

            for (String day : daysOfWeek) {
                int dayOfWeek = getDayOfWeek(day.trim().toLowerCase());

                // Skip invalid days
                if (dayOfWeek == -1) {
                    continue;
                }

                Calendar scheduledReminderTime = (Calendar) reminderTime.clone();

                // Calculate the next occurrence of the reminder day
                while (scheduledReminderTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek || scheduledReminderTime.before(now)) {
                    scheduledReminderTime.add(Calendar.DATE, 1); // Move to the next week
                }

                // Check if the scheduled time is before now (for the current day)
                if (scheduledReminderTime.get(Calendar.DAY_OF_WEEK) == now.get(Calendar.DAY_OF_WEEK) &&
                        scheduledReminderTime.getTimeInMillis() < now.getTimeInMillis()) {
                    // Already missed today's reminder, but keep it for next week
                    continue; // Skip scheduling for this day
                }

                // Create the intent and pending intent
                Intent intent = new Intent(Notification.this, NotificationReceiver.class);
                intent.putExtra("reminderMessage", username + ", Don't forget your " + reminderKey + " routine at " + reminder.getTime() + "!");

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        Notification.this,
                        (reminderKey + dayOfWeek).hashCode(),  // Unique request code per reminder day
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledReminderTime.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to schedule notification.", Toast.LENGTH_SHORT).show();
        }
    }


    // Helper method to map day names to Calendar.DAY_OF_WEEK values
    private int getDayOfWeek(String day) {
        switch (day) {
            case "sunday":
                return Calendar.SUNDAY;
            case "monday":
                return Calendar.MONDAY;
            case "tuesday":
                return Calendar.TUESDAY;
            case "wednesday":
                return Calendar.WEDNESDAY;
            case "thursday":
                return Calendar.THURSDAY;
            case "friday":
                return Calendar.FRIDAY;
            case "saturday":
                return Calendar.SATURDAY;
            default:
                return -1;
        }
    }


}
