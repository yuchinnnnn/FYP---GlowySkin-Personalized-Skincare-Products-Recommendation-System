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
                remindersList.remove(position);  // Remove the item from the list
                notificationAdapter.notifyItemRemoved(position);  // Notify the adapter about item removal
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
        DatabaseReference remindersRef = FirebaseDatabase.getInstance().getReference("Reminders").child(userId);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue(String.class);
                if (username != null) {
                    notificationAdapter.updateUsername(username); // Add a method in the adapter to update the username
                    fetchReminder(remindersRef, "morning_reminder", username);
                    fetchReminder(remindersRef, "afternoon_reminder", username);
                    fetchReminder(remindersRef, "night_reminder", username);
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

    private void fetchReminder(DatabaseReference remindersRef, String reminderKey, String username) {
        remindersRef.child(reminderKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MorningReminders reminder = dataSnapshot.getValue(MorningReminders.class);

                if (reminder != null) {
                    Log.d("Reminder", "Title: " + reminder.getTitle());
                    Log.d("Reminder", "Time: " + reminder.getTime());
                    Log.d("Reminder", "Days: " + reminder.getDays());

                    reminder.setKey(reminderKey);  // Set the key here
                    remindersList.add(reminder);
                    notificationAdapter.notifyDataSetChanged();
                    scheduleNotification(reminder, username, reminderKey);
                } else {
                    Toast.makeText(Notification.this, "No reminder found for " + reminderKey, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Notification.this, "Failed to fetch " + reminderKey, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleNotification(Reminder reminder, String username, String reminderKey) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            // Parse the reminder time
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTime(sdf.parse(reminder.getTime()));

            // Adjust the time for the reminder (subtract 10 minutes)
            reminderTime.add(Calendar.MINUTE, -10);

            // Split the days of the week
            String[] daysOfWeek = reminder.getDays().split(",");

            for (String day : daysOfWeek) {
                int dayOfWeek = getDayOfWeek(day.trim().toLowerCase());

                // Skip invalid days
                if (dayOfWeek == -1) {
                    continue;
                }

                Calendar now = Calendar.getInstance();
                Calendar scheduledReminderTime = (Calendar) reminderTime.clone();

                // Calculate the next occurrence of the reminder day
                while (scheduledReminderTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek || scheduledReminderTime.before(now)) {
                    scheduledReminderTime.add(Calendar.DATE, 1);
                }

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
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledReminderTime.getTimeInMillis(), pendingIntent);
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
