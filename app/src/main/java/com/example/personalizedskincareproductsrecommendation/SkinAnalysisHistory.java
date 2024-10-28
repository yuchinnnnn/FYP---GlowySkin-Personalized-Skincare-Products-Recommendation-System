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

public class SkinAnalysisHistory extends Fragment {

    private ListView skinAnalysisListView;
    private SkinAnalysisAdapter skinAnalysisListAdapter;
    private TextView noHistoryMessage;
    private List<SkinAnalysisEntry> skinAnalysisEntries;
    private DatabaseReference databaseReference;
    private String userId;

    public static SkinAnalysisHistory newInstance(String userId) {
        SkinAnalysisHistory fragment = new SkinAnalysisHistory();
        Bundle args = new Bundle();
        args.putString("ARG_USER_ID", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_skin_analysis_history, container, false);

        // Initialize views
        skinAnalysisListView = rootView.findViewById(R.id.skin_analysis_history_list);
        skinAnalysisListView.setDivider(null);
        skinAnalysisEntries = new ArrayList<>();
        skinAnalysisListAdapter = new SkinAnalysisAdapter(getContext(), skinAnalysisEntries);
        skinAnalysisListView.setAdapter(skinAnalysisListAdapter);

        noHistoryMessage = rootView.findViewById(R.id.no_history_message);

        // Get user ID from arguments
        if (getArguments() != null) {
            userId = getArguments().getString("ARG_USER_ID");
            Log.d("SkinAnalysisHistory", "User ID: " + userId);
        } else {
            Log.e("SkinAnalysisHistory", "Arguments are null!");
        }

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("SkinAnalysis").child(userId);
        fetchSkinAnalysisData();

        return rootView;
    }

    private void fetchSkinAnalysisData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                skinAnalysisEntries.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot analysisSnapshot : dataSnapshot.getChildren()) {
                        String userId = analysisSnapshot.child("userId").getValue(String.class);
                        String keyId = analysisSnapshot.getKey(); // Unique key for each
                        String timestamp = analysisSnapshot.child("uploadedDateTime").getValue(String.class); // Ensure the correct field is fetched
                        String imageUrl = analysisSnapshot.child("imageUrl").getValue(String.class);
                        String skinType = analysisSnapshot.child("skinType").getValue(String.class);
                        Log.d("SkinAnalysisHistory", "Analysis ID: " + keyId + ", Timestamp: " + timestamp + ", User ID: " + userId);

                        // Extract skin condition data
                        float acne = analysisSnapshot.child("skinCondition/acne").getValue(Float.class);
                        float darkCircle = analysisSnapshot.child("skinCondition/darkCircle").getValue(Float.class);
                        float darkSpot = analysisSnapshot.child("skinCondition/darkSpot").getValue(Float.class);
                        float pores = analysisSnapshot.child("skinCondition/pores").getValue(Float.class);
                        float redness = analysisSnapshot.child("skinCondition/redness").getValue(Float.class);
                        float wrinkles = analysisSnapshot.child("skinCondition/wrinkles").getValue(Float.class);

                        // Create SkinCondition object
                        SkinCondition skinCondition = new SkinCondition(acne, redness, wrinkles, darkSpot, darkCircle, pores);

                        // Create SkinAnalysisEntry object
                        SkinAnalysisEntry entry = new SkinAnalysisEntry(keyId, timestamp, userId, imageUrl, skinType, skinCondition);
                        skinAnalysisEntries.add(entry);
                    }

                    Log.d("SkinAnalysisHistory", "Entries count: " + skinAnalysisEntries.size());
                    skinAnalysisListAdapter.notifyDataSetChanged();
                    // Handle visibility
                    if (skinAnalysisEntries.isEmpty()) {
                        skinAnalysisListView.setVisibility(View.GONE);
                        noHistoryMessage.setVisibility(View.VISIBLE);
                    } else {
                        skinAnalysisListView.setVisibility(View.VISIBLE);
                        noHistoryMessage.setVisibility(View.GONE);
                    }
                } else {
                    skinAnalysisListView.setVisibility(View.GONE);
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

