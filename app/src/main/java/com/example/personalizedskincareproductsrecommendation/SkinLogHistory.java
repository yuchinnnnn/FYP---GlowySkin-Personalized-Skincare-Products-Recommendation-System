package com.example.personalizedskincareproductsrecommendation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SkinLogHistory extends Fragment {

    private ListView skinLogListView;
    private SkinLogAdapter skinLogListAdapter;
    private TextView noHistoryMessage;
    private List<SkinLogEntry> skinLogEntries;
    private DatabaseReference databaseReference;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_skin_log_history, container, false);

        // Initialize views
        skinLogListView = rootView.findViewById(R.id.skin_log_history_list);
        skinLogListView.setDivider(null);
        skinLogEntries = new ArrayList<>();
        skinLogListAdapter = new SkinLogAdapter(getContext(), skinLogEntries);
        skinLogListView.setAdapter(skinLogListAdapter);

        noHistoryMessage = rootView.findViewById(R.id.no_history_message);

        // Get user ID from arguments
        if (getArguments() != null) {
            userId = getArguments().getString("ARG_USER_ID");
            Log.d("SkinLogHistory", "User ID: " + userId);
        } else {
            Log.e("SkinLogHistory", "Arguments are null!");
        }

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinLog").child(userId);
        fetchSkinLogs();

        return rootView;
    }

    private void fetchSkinLogs() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                skinLogEntries.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot logSnapshot : dataSnapshot.getChildren()) {
                        String logId = logSnapshot.getKey(); // Use logId directly from the key
                        String timestamp = logSnapshot.child("timestamp").getValue(String.class);
                        String userId = logSnapshot.child("userId").getValue(String.class);
                        Log.d("SkinLogHistory", "Log ID: " + logId + ", Timestamp: " + timestamp + ", User ID: " + userId);

                        // Extract selfies URLs
                        String leftSelfieUrl = logSnapshot.child("selfies/left").getValue(String.class);
                        String rightSelfieUrl = logSnapshot.child("selfies/right").getValue(String.class);
                        String frontSelfieUrl = logSnapshot.child("selfies/front").getValue(String.class);
                        String neckSelfieUrl = logSnapshot.child("selfies/neck").getValue(String.class);

                        SkinLogEntry entry = new SkinLogEntry(logId, timestamp, userId, leftSelfieUrl, rightSelfieUrl, frontSelfieUrl, neckSelfieUrl);
                        skinLogEntries.add(entry);
                    }

                    Log.d("SkinLogHistory", "Entries count: " + skinLogEntries.size());
                    skinLogListAdapter.notifyDataSetChanged();

                    // Handle visibility
                    if (skinLogEntries.isEmpty()) {
                        skinLogListView.setVisibility(View.GONE);
                        noHistoryMessage.setVisibility(View.VISIBLE);
                    } else {
                        skinLogListView.setVisibility(View.VISIBLE);
                        noHistoryMessage.setVisibility(View.GONE);
                    }
                } else {
                    skinLogListView.setVisibility(View.GONE);
                    noHistoryMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("SkinLogHistory", "Database error: " + databaseError.getMessage());
            }
        });
    }
}


