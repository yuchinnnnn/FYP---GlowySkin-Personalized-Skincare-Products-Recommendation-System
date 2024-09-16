package com.example.personalizedskincareproductsrecommendation;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Reminder> remindersList;
    private String username;
    private SimpleDateFormat timeFormat;

    public NotificationAdapter(List<Reminder> remindersList, String username) {
        this.remindersList = remindersList;
        this.username = username;
        this.timeFormat = new SimpleDateFormat("HH:mm"); // To compare reminder times
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = remindersList.get(position);

        // Check if the reminder is for today
        if (isReminderForToday(reminder)) {
            holder.reminderTitle.setText(reminder.getTitle());
            holder.reminderText.setText(getFormattedReminderText(reminder));
            holder.sunImageView.setImageResource(getIconForReminderKey(reminder.getKey()));

            // Check if the reminder time has passed
            if (hasReminderTimePassed(reminder)) {
                // Change text color to indicate the reminder has passed
                holder.reminderText.setTextColor(Color.RED);
            } else {
                holder.reminderText.setTextColor(Color.BLACK);
            }
        } else {
            // Hide the item if the reminder is not for today
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sunImageView;
        TextView reminderTitle, reminderText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sunImageView = itemView.findViewById(R.id.sun);
            reminderTitle = itemView.findViewById(R.id.reminder_title);
            reminderText = itemView.findViewById(R.id.text);
        }
    }

    private int getIconForReminderKey(String reminderKey) {
        if (reminderKey == null) {
            Log.e("NotificationAdapter", "Reminder key is null");
            return R.drawable.nothing; // Default icon
        }

        switch (reminderKey) {
            case "morning_reminder":
                return R.drawable.morning;
            case "afternoon_reminder":
                return R.drawable.afternoon;
            case "night_reminder":
                return R.drawable.night;
            default:
                return R.drawable.nothing; // Default icon
        }
    }

    public void updateUsername(String username) {
        this.username = username;
        notifyDataSetChanged();
    }

    private boolean isReminderForToday(Reminder reminder) {
        String[] daysOfWeek = reminder.getDays().split(",");
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        for (String day : daysOfWeek) {
            if (getDayOfWeek(day.trim().toLowerCase()) == today) {
                return true;
            }
        }
        return false;
    }

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

    private boolean hasReminderTimePassed(Reminder reminder) {
        try {
            Calendar now = Calendar.getInstance();
            Date reminderTime = timeFormat.parse(reminder.getTime());
            Calendar reminderCalendar = Calendar.getInstance();
            reminderCalendar.setTime(reminderTime);
            reminderCalendar.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
            reminderCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

            return now.after(reminderCalendar);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getFormattedReminderText(Reminder reminder) {
        String reminderText = username + ", Don't forget your " + reminder.getKey() + " routine at " + reminder.getTime();
        if (hasReminderTimePassed(reminder)) {
            Spannable spannable = new SpannableString(reminderText + " (Passed)");
            spannable.setSpan(new ForegroundColorSpan(Color.RED), reminderText.length(), spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spannable.toString();
        }
        return reminderText;
    }
}
