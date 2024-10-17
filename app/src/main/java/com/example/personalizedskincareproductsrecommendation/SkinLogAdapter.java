package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SkinLogAdapter extends ArrayAdapter<SkinLogEntry> {
    private final Context context;
    private final List<SkinLogEntry> entries;

    public SkinLogAdapter(Context context, List<SkinLogEntry> entries) {
        super(context, R.layout.skin_log_list_item, entries);
        this.context = context;
        this.entries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.skin_log_list_item, parent, false);

        // Get references to views
        TextView dateView = rowView.findViewById(R.id.date_text_view);
        TextView timeView = rowView.findViewById(R.id.time_text_view);
        ImageView viewButton = rowView.findViewById(R.id.view_button);

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SkinLogDetails.class);
                intent.putExtra("ARG_USER_ID", entries.get(position).getUserId());
                intent.putExtra("ARG_DATE", entries.get(position).getTimestamp());
                intent.putExtra("ARG_TIME", entries.get(position).getTimestamp());
                context.startActivity(intent);
            }
        });

        // Get the SkinLogEntry for this position
        SkinLogEntry entry = entries.get(position);

        // Assuming entry.getTimestamp() returns a string in "yyyy-MM-dd HH:mm:ss" format
        String timestamp = entry.getTimestamp();

        // Format the timestamp to separate date and time
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = originalFormat.parse(timestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()); // Use hh for 12-hour format and add "a" for AM/PM

            // Set the formatted date and time into the views
            dateView.setText("Date: " + dateFormat.format(date));
            timeView.setText("Time: " + timeFormat.format(date));

        } catch (Exception e) {
            e.printStackTrace();
            // Handle the case where the timestamp is not in the expected format
            dateView.setText("Invalid date");
            timeView.setText("Invalid time");
        }

        return rowView;
    }

}

