package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class TipDetails extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView;
    private ImageView back;
    private ViewPager viewPager;
    private ArrayList<String> imageUrls;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ImageSliderAdapter imageSliderAdapter;
    private String tipId;
    private LinearLayout sliderDots;
    private int dotsCount;
    private ImageView[] dots;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_details);

        // Initialize UI elements
        titleTextView = findViewById(R.id.title);
        descriptionTextView = findViewById(R.id.description);
        viewPager = findViewById(R.id.viewPager);

        // Initialize Firebase references
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Initialize Firebase database reference

        imageUrls = new ArrayList<>();

        // Get the tipId passed from the previous activity/fragment
        String tipId = getIntent().getStringExtra("tipId");
        if (tipId != null) {
            fetchTipsDetails(tipId);
        } else {
            Log.e("TipDetails", "tipId is null!");
        }

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchTipsDetails(String tipId) {
        // Assuming 'tipId' is passed to fetch a specific tip
        DatabaseReference tipReference = databaseReference.child("SkincareTips").child(tipId);

        tipReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch title
                    String title = dataSnapshot.child("title").getValue(String.class);
                    titleTextView.setText(title);

                    // Fetch description
                    String description = dataSnapshot.child("description").getValue(String.class);
                    descriptionTextView.setText(description);

                    // Check if 'images' exists before accessing
                    if (dataSnapshot.child("images").exists()) {
                        imageUrls.clear();  // Clear list before adding new URLs
                        for (DataSnapshot snapshot : dataSnapshot.child("images").getChildren()) {
                            String imageUrl = snapshot.getValue(String.class);
                            imageUrls.add(imageUrl);
                        }
                        setUpViewPager();
                    } else {
                        // Handle case when no images are present
                        Log.d("TipsDetails", "No images available for this tip.");
                    }
                } else {
                    Log.d("TipsDetails", "Tip data not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("TipsDetails", "Failed to load data.", databaseError.toException());
            }
        });
    }

    private void setUpViewPager() {
        imageSliderAdapter = new ImageSliderAdapter(this, imageUrls);
        viewPager.setAdapter(imageSliderAdapter);

        sliderDots = findViewById(R.id.sliderDots);
        dotsCount = imageSliderAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            sliderDots.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(TipDetails.this, R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(TipDetails.this, R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }
}
