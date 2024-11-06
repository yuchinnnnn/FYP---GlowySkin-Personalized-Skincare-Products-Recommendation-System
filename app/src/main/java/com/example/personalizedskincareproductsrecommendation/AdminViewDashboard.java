package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminViewDashboard extends AppCompatActivity {
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private ImageView back;
    private TextView numUsersTextView, oilySkinUsersTextView, drySkinUsersTextView, normalSkinUsersTextView, feedbackTextView;
    private PieChart pieChart;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_dashboard);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
        back = findViewById(R.id.back);
        numUsersTextView = findViewById(R.id.num_users);
        oilySkinUsersTextView = findViewById(R.id.oily_skin_users);
        drySkinUsersTextView = findViewById(R.id.dry_skin_users);
        normalSkinUsersTextView = findViewById(R.id.normal_skin_users);
//        feedbackTextView = findViewById(R.id.feedbackTextView);
        pieChart = findViewById(R.id.pieChart);

        back.setOnClickListener(view -> {
            Intent intent = new Intent(AdminViewDashboard.this, AdminDashboard.class);
            intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
            startActivity(intent);
        });

        // Fetch data from Firebase
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference skinTypesReference = FirebaseDatabase.getInstance().getReference("SkinQuiz");

        // Fetch user count
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int counter = (int) dataSnapshot.getChildrenCount();
                String userCounter = String.valueOf(counter);
                numUsersTextView.setText(userCounter);
                Log.d("User Counter", userCounter);

                // Now fetch skin types after counting users
                countSkinTypes(skinTypesReference);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminViewDashboard", "Failed to read users.", databaseError.toException());
            }
        });
    }

    private void countSkinTypes(DatabaseReference skinTypesReference) {
        skinTypesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int oilyCount = 0;
                int dryCount = 0;
                int normalCount = 0;

                for (DataSnapshot skinTypeSnapshot : dataSnapshot.getChildren()) {
                    String skinType = skinTypeSnapshot.child("result").getValue(String.class);
                    if (skinType.contains("Oil")) {
                        oilyCount++;
                    } else if (skinType.contains("Dry")) {
                        dryCount++;
                    } else if (skinType.contains("Normal")) {
                        normalCount++;
                    }
                }

                // Update the UI with the skin type counts
                oilySkinUsersTextView.setText(String.valueOf(oilyCount));
                drySkinUsersTextView.setText(String.valueOf(dryCount));
                normalSkinUsersTextView.setText(String.valueOf(normalCount));
                Log.d("Skin Type Counts", "Oily: " + oilyCount + ", Dry: " + dryCount + ", Normal: " + normalCount);

                // Now populate the Pie Chart
                populatePieChart(oilyCount, dryCount, normalCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AdminViewDashboard", "Failed to read skin types.", databaseError.toException());
            }
        });
    }

    private void populatePieChart(int oilyCount, int dryCount, int normalCount) {
        // Prepare entries for the pie chart
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(oilyCount, "Oily Skin"));
        pieEntries.add(new PieEntry(dryCount, "Dry Skin"));
        pieEntries.add(new PieEntry(normalCount, "Normal Skin"));

        // Create a PieDataSet
        PieDataSet dataSet = new PieDataSet(pieEntries, "Skin Types Distribution");
        dataSet.setColors(Color.parseColor("#D8E5F7"), Color.parseColor("#FDF1C9"),Color.parseColor("#FBC4BF"));
        dataSet.setValueTextSize(16f);
        dataSet.setSliceSpace(3f);

        // Create a PieData object with the dataset
        PieData data = new PieData(dataSet);

        // Set the data in the pie chart and refresh it
        pieChart.setData(data);
        pieChart.invalidate(); // Refresh the chart

        // Optional: Customize pie chart
        pieChart.getDescription().setEnabled(false); // Remove the description label
        pieChart.setCenterText("Skin Types"); // Set a center label
        pieChart.animateY(1000); // Add some animation
    }
}
