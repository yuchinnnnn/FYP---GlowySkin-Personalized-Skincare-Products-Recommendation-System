package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminViewFeedback extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private ImageView back;
    private RecyclerView feedbackRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private List<Feedback> feedbackList; // Holds all feedback
    private List<Feedback> filteredFeedbackList; // Holds filtered feedback
    private Spinner ratingFilterSpinner, dateSortSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_feedback); // Ensure this matches your layout file name

        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminViewFeedback.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        // Initialize the RecyclerView
        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);
        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the Spinners
        ratingFilterSpinner = findViewById(R.id.ratingFilterSpinner);
        dateSortSpinner = findViewById(R.id.dateSortSpinner);

        // Initialize feedback lists
        feedbackList = new ArrayList<>(); // Initialize the feedback list
        filteredFeedbackList = new ArrayList<>(); // Initialize the filtered feedback list
        feedbackAdapter = new FeedbackAdapter(filteredFeedbackList);
        feedbackRecyclerView.setAdapter(feedbackAdapter);

        // Setup Spinners
        setupSpinners();

        // Load feedback data
        loadFeedbackData();
    }

    private void loadFeedbackData() {
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("Feedback");
        feedbackRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                feedbackList.clear();
                for (DataSnapshot feedbackSnapshot : snapshot.getChildren()) {
                    Feedback feedback = feedbackSnapshot.getValue(Feedback.class);
                    if (feedback != null) {
                        feedbackList.add(feedback);
                    }
                }
                // Update filtered list and notify the adapter
                filteredFeedbackList.clear();
                filteredFeedbackList.addAll(feedbackList);
                feedbackAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to read feedback: " + error.getMessage());
            }
        });
    }

    private void setupSpinners() {
        // Setup Spinner for Rating Filter
        ArrayAdapter<CharSequence> ratingAdapter = ArrayAdapter.createFromResource(this,
                R.array.rating_filter_options, android.R.layout.simple_spinner_item);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingFilterSpinner.setAdapter(ratingAdapter);

        ratingFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRating = parent.getItemAtPosition(position).toString();
                filterFeedbackByRating(selectedRating);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Reset to show all feedback if nothing is selected
                filteredFeedbackList.clear();
                filteredFeedbackList.addAll(feedbackList);
                feedbackAdapter.notifyDataSetChanged();
            }
        });

        // Setup Spinner for Date Sort
        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this,
                R.array.date_sort_options, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSortSpinner.setAdapter(dateAdapter);

        dateSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = parent.getItemAtPosition(position).toString();
                sortFeedbackByDate(selectedSort);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterFeedbackByRating(String rating) {
        // Ensure filteredFeedbackList is initialized
        if (filteredFeedbackList == null) {
            filteredFeedbackList = new ArrayList<>();
        }
        filteredFeedbackList.clear();

        if (rating.equals("All Ratings")) {
            // Show all feedback if "All Ratings" is selected
            filteredFeedbackList.addAll(feedbackList);
        } else {
            try {
                // Try to parse the rating into an integer
                int filterRating = Integer.parseInt(rating);
                for (Feedback feedback : feedbackList) {
                    if (feedback.getRating() == filterRating) {
                        filteredFeedbackList.add(feedback);
                    }
                }
            } catch (NumberFormatException e) {
                // Log or handle the exception, if the rating string cannot be parsed to an integer
                Log.e("filterFeedbackByRating", "Invalid rating format: " + rating, e);
            }
        }
        feedbackAdapter.notifyDataSetChanged();
    }

    private void sortFeedbackByDate(String sortOption) {
        if (sortOption.equals("Newest First")) {
            // Sort by date descending
            filteredFeedbackList.sort((feedback1, feedback2) -> feedback2.getDate().compareTo(feedback1.getDate()));
        } else if (sortOption.equals("Oldest First")) {
            // Sort by date ascending
            filteredFeedbackList.sort(Comparator.comparing(Feedback::getDate));
        }
        feedbackAdapter.notifyDataSetChanged();
    }
}
