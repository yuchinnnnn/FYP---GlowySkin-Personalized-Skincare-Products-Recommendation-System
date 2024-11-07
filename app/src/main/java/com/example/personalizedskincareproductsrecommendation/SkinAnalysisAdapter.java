package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

public class SkinAnalysisAdapter extends ArrayAdapter<SkinAnalysisEntry> {
    private final Context context;
    private final List<SkinAnalysisEntry> entries;

    public SkinAnalysisAdapter(Context context, List<SkinAnalysisEntry> entries) {
        super(context, R.layout.skin_analysis_list_item, entries);
        this.context = context;
        this.entries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.skin_analysis_list_item, parent, false);

        // Get references to views
        TextView dateView = rowView.findViewById(R.id.date_text_view);
        TextView timeView = rowView.findViewById(R.id.time_text_view);
        ImageView viewButton = rowView.findViewById(R.id.view_button);

        // Get the SkinAnalysisEntry for this position
        SkinAnalysisEntry entry = entries.get(position);

        // Set userId to the intent when viewButton is clicked
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SkinAnalysisResult.class);
                intent.putExtra("ARG_USER_ID", entry.getUserId());
                intent.putExtra("ARG_ANALYSIS_ID", entry.getKeyId());
                intent.putExtra("ARG_DATE", entry.getUploadedDateTime());
                intent.putExtra("ARG_TIME", entry.getUploadedDateTime());
                Log.d("SkinAnalysisAdapter", "User ID: " + entry.getUserId());
                context.startActivity(intent);
            }
        });

        // Format and set the date and time views
        String timestamp = entry.getUploadedDateTime();
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = originalFormat.parse(timestamp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

            dateView.setText("Date: " + dateFormat.format(date));
            timeView.setText("Time: " + timeFormat.format(date));

        } catch (Exception e) {
            e.printStackTrace();
            dateView.setText("Invalid date");
            timeView.setText("Invalid time");
        }

        return rowView;
    }

}

