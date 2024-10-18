package com.example.personalizedskincareproductsrecommendation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AfternoonReminder extends AppCompatActivity {

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
        setContentView(R.layout.activity_afternoon_reminder);

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
        setButton.setEnabled(false);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });

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

        // Check if the time is in the morning session
        if (hour <= 12 || hour >= 17) {
            Toast.makeText(this, "Please select a time before 17PM and after 12PM", Toast.LENGTH_SHORT).show();
            return;
        }

        String time = String.format("%02d:%02d", hour, minute);

        // Retrieve the selected days
        StringBuilder markedDays = new StringBuilder();
        if (sun.isChecked()) {
            markedDays.append("Sunday,");
        }
        if (mon.isChecked()) {
            markedDays.append("Monday,");
        }
        if (tue.isChecked()) {
            markedDays.append("Tuesday,");
        }
        if (wed.isChecked()) {
            markedDays.append("Wednesday,");
        }
        if (thurs.isChecked()) {
            markedDays.append("Thursday,");
        }
        if (fri.isChecked()) {
            markedDays.append("Friday,");
        }
        if (sat.isChecked()) {
            markedDays.append("Saturday,");
        }

        // Remove the last comma if it exists
        if (markedDays.length() > 0 && markedDays.charAt(markedDays.length() - 1) == ',') {
            markedDays.deleteCharAt(markedDays.length() - 1);
        }

        // Prepare data to be saved
        Map<String, Object> reminderData = new HashMap<>();
        reminderData.put("title", "Afternoon Reminder");
        reminderData.put("time", time);
        reminderData.put("days", markedDays.toString());
        reminderData.put("isRemoved", false);
        reminderData.put("isPassed", false);

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Reminders").child(userId).child("afternoon_reminder");

        // Save to Firebase
        databaseReference.setValue(reminderData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AfternoonReminder.this, "Reminder set successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AfternoonReminder.this, "Failed to set reminder", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

