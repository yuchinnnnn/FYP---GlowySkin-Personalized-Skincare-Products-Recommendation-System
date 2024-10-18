package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MorningReminder extends AppCompatActivity {

    // Day buttons
    ToggleButton sun, mon, tue, wed, thurs, fri, sat;
    TimePicker timepicker;
    Button setButton, cancel;
    ImageView back;
    Switch reminder;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String userId;

    private static final String ARG_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morning_reminder);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Reminders");

        sun = findViewById(R.id.sun);
        mon = findViewById(R.id.mon);
        tue = findViewById(R.id.tue);
        wed = findViewById(R.id.wed);
        thurs = findViewById(R.id.thurs);
        fri = findViewById(R.id.fri);
        sat = findViewById(R.id.sat);

        timepicker = findViewById(R.id.timepicker);
        timepicker.setIs24HourView(true);

        setButton = findViewById(R.id.set_button);
        // Initially set the "Set" button to disabled
        setButton.setEnabled(false);
//        setButton.setOnClickListener(v -> saveReminderToFirebase());
        setButton.setOnClickListener(v -> showConfirmDialog());

        cancel = findViewById(R.id.cancel_button);
        cancel.setOnClickListener(v -> finish());

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());


        reminder = findViewById(R.id.reminder);
        reminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setButton.setEnabled(true);
            } else {
                setButton.setEnabled(false);
            }
        });
    }


    private void showConfirmDialog() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle("Are you sure?");
        sweetAlertDialog.setContentText("Do you want to set the reminder?");
        sweetAlertDialog.setConfirmText("Yes");
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
                saveReminderToFirebase();
                finish();
            }
        });
        sweetAlertDialog.setCancelText("No");
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismissWithAnimation();
    }
        });
        sweetAlertDialog.show();
    }

    private void saveReminderToFirebase() {
        // Retrieve the selected time
        int hour = timepicker.getHour();
        int minute = timepicker.getMinute();

        // Validate selected time
        if (hour < 6 || hour >= 12) {
            Toast.makeText(this, "Please select a time between 6 AM and 11:59 AM", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = String.format("%02d:%02d", hour, minute);

        // Retrieve the selected days
        List<String> selectedDays = new ArrayList<>();
        if (sun.isChecked()) selectedDays.add("Sunday");
        if (mon.isChecked()) selectedDays.add("Monday");
        if (tue.isChecked()) selectedDays.add("Tuesday");
        if (wed.isChecked()) selectedDays.add("Wednesday");
        if (thurs.isChecked()) selectedDays.add("Thursday");
        if (fri.isChecked()) selectedDays.add("Friday");
        if (sat.isChecked()) selectedDays.add("Saturday");

        // Join the selected days
        String daysString = TextUtils.join(",", selectedDays);

        // Prepare data to be saved
        Map<String, Object> reminderData = new HashMap<>();
        reminderData.put("title", "Morning Reminder");
        reminderData.put("time", time);
        reminderData.put("days", daysString);
        reminderData.put("isRemoved", false);
        reminderData.put("isPassed", false);

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Save to Firebase
        databaseReference.child(userId).child("morning_reminder").setValue(reminderData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MorningReminder.this, "Reminder set successfully", Toast.LENGTH_SHORT).show();
                finish(); // Optionally finish the activity after success
            } else {
                Log.e("MorningReminder", "Failed to set reminder", task.getException());
                Toast.makeText(MorningReminder.this, "Failed to set reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

