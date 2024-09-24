package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class AdminDashboard extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout sliderDots;
    private int dotsCount;
    private ImageView[] dots;
    private TextView greeting;
    private Button addContent, manageUserProfile, viewFeedback, editProducts;
    private String userId;
    public static final String ARG_USER_ID = "userId";

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // for sidemenu
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MyTimerTask(), 2000, 4000);

        greeting = findViewById(R.id.greeting);

        // Retrieve user ID from Intent
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            // Handle the case where userId is not passed or retrieved
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            // You can choose to finish the activity or handle it accordingly
            finish();
            return;
        }

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        // Change reference to the new SkinGoals table
        if (userId != null) {
            // Retrieve username from Users table
            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
            usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String username = snapshot.child("username").getValue(String.class);
                    if (username != null) {
                        greeting.setText("Welcome, " + username);
                    } else {
                        greeting.setText("Welcome, Admin");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }

        addContent = findViewById(R.id.add_content);

        addContent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminAddContent.class);
            intent.putExtra(AdminAddContent.ARG_USER_ID, userId);
            startActivity(intent);
        });

        manageUserProfile = findViewById(R.id.manage_user_profile);

        manageUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminManageProfile.class);
            intent.putExtra(AdminManageProfile.ARG_USER_ID, userId);
            startActivity(intent);
        });

        viewFeedback = findViewById(R.id.view_feedback);

        viewFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminViewFeedback.class);
            intent.putExtra(AdminViewFeedback.ARG_USER_ID, userId);
            startActivity(intent);
        });

        editProducts = findViewById(R.id.edit_product);

        editProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AdminEditProducts.class);
            intent.putExtra(AdminEditProducts.ARG_USER_ID, userId);
            startActivity(intent);
        });

        viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);

        viewPager.setAdapter(viewPagerAdapter);
        sliderDots = findViewById(R.id.sliderDots);

        dotsCount = viewPagerAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            sliderDots.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(AdminDashboard.this, R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(AdminDashboard.this, R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // for sidemenu
        navigationView = findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return navigateTo(item.getItemId());
            }
        });


        // Hide the default ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar = findViewById(R.id.menu);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    private boolean navigateTo(int itemId) {
        Intent intent;

        if (itemId == R.id.home_menu) {
            intent = new Intent(this, AdminDashboard.class);
            Log.d(TAG, "Navigating to Dashboard");
        } else if (itemId == R.id.dashboard_menu) {
            intent = new Intent(this, AdminViewDashboard.class);
        } else if (itemId == R.id.skincare_tips) {
            intent = new Intent(this, AdminAddContent.class);
        } else if (itemId == R.id.manage_account) {
            intent = new Intent(this, AdminManageProfile.class);
        } else if (itemId == R.id.feedback) {
            intent = new Intent(this, AdminViewFeedback.class);
        } else if (itemId == R.id.logout) {
            mAuth.signOut();
            intent = new Intent(this, Login.class);
        } else if (itemId == R.id.account) {
            intent = new Intent(this, AdminAccountSetting.class);
        } else {
            Log.d(TAG, "Unknown menu item selected");
            return false;
        }

        startActivity(intent);
        drawerLayout.closeDrawers(); // Close the drawer after navigating
        return true;
    }


    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            AdminDashboard.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem() == 0) {
                        viewPager.setCurrentItem(1);
                    } else if (viewPager.getCurrentItem() == 1) {
                        viewPager.setCurrentItem(2);
                    } else {
                        viewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }
}