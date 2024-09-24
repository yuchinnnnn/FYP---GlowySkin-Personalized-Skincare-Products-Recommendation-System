package com.example.personalizedskincareproductsrecommendation;

import static com.example.personalizedskincareproductsrecommendation.AdminDashboard.ARG_USER_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminManageProfile extends AppCompatActivity {

    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private ImageView back;
    private DatabaseReference userReference;
    private ProfileViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_profile);

        // Initialize back button
        back = findViewById(R.id.back);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(AdminManageProfile.this, AdminDashboard.class);
            startActivity(intent);
        });

        // Initialize the ViewPager and TabLayout
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        // Set up ViewPager
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ProfileViewPagerAdapter adapter = new ProfileViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments for Active and Deactivated Users
        adapter.addFragment(new ActiveUsers(), "Active Users");
        adapter.addFragment(new DeactivatedUser(), "Deactivated Users");
        adapter.addFragment(new ToApproveUser(), "To Approve Users");

        viewPager.setAdapter(adapter);
    }
}
