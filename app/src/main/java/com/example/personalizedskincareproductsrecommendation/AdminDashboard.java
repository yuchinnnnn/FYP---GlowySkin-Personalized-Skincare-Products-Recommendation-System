package com.example.personalizedskincareproductsrecommendation;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private Button addContent, manageUserProfile, viewDashboard, editProducts;
    private String userId;
    public static final String ARG_USER_ID = "userId";

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // for sidemenu
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

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
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

        // Retrieve the admin's username
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

        // Initialize buttons and their click listeners
        initButtons();

        // Initialize ViewPager and Dots Indicator
        setupViewPager();

        // Set up the Navigation Drawer
        setupDrawer();
    }

    private void initButtons() {
        addContent = findViewById(R.id.add_content);
        addContent.setOnClickListener(v -> openActivity(AdminAddContent.class));

        manageUserProfile = findViewById(R.id.manage_user_profile);
        manageUserProfile.setOnClickListener(v -> openActivity(AdminManageProfile.class));

        viewDashboard = findViewById(R.id.view_dashboard);
        viewDashboard.setOnClickListener(v -> openActivity(AdminViewDashboard.class));

        editProducts = findViewById(R.id.edit_product);
        editProducts.setOnClickListener(v -> openActivity(AdminManageProducts.class));
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(AdminDashboard.this, activityClass);
        intent.putExtra(AdminAddContent.ARG_USER_ID, userId);
        intent.putExtra(AdminManageProfile.ARG_USER_ID, userId);
        intent.putExtra(AdminViewDashboard.ARG_USER_ID, userId);
        intent.putExtra(AdminManageProducts.ARG_USER_ID, userId);
        startActivity(intent);
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        sliderDots = findViewById(R.id.sliderDots);
        dotsCount = viewPagerAdapter.getCount();
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
                    dots[i].setImageDrawable(ContextCompat.getDrawable(AdminDashboard.this, R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(AdminDashboard.this, R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.menu); // Initialize toolbar here

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24);

        // Set up ActionBarDrawerToggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open, R.string.navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            Log.d("AdminDashboard", "Menu item selected: " + item.getTitle());
            Toast.makeText(this, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            handleMenuClick(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true; // Return true to indicate that the event was handled
        });

        toolbar.setNavigationOnClickListener(v -> {
            Log.d("AdminDashboard", "Navigation icon clicked");
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    private void handleMenuClick(int id) {
        switch (id) {
            case R.id.home_menu:
                Log.d("AdminDashboard", "Home clicked");
                openActivity(AdminDashboard.class);
                break;
            case R.id.dashboard_menu:
                Log.d("AdminDashboard", "Dashboard clicked");
                Toast.makeText(this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
                openActivity(AdminViewDashboard.class);
                break;
            case R.id.skincare_tips:
                Log.d("AdminDashboard", "Manage Content clicked");
                openActivity(AdminManageContent.class);
                break;
            case R.id.manage_product:
                Log.d("AdminDashboard", "Manage Products clicked");
                openActivity(AdminManageProducts.class);
                break;
            case R.id.manage_account:
                Log.d("AdminDashboard", "Manage User Account clicked");
                openActivity(AdminManageProfile.class);
                break;
            case R.id.feedback:
                Log.d("AdminDashboard", "Feedback clicked");
                openActivity(AdminViewFeedback.class);
                break;
            case R.id.logout:
                Log.d("AdminDashboard", "Logout clicked");
                startActivity(new Intent(this, Login.class));
                break;
            case R.id.Account:
                Log.d("AdminDashboard", "Account clicked");
                openActivity(AdminAccountSetting.class);
                break;
            default:
                Log.d("AdminDashboard", "Unknown item clicked");
                break;
        }
    }

    public class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(() -> {
                int currentItem = viewPager.getCurrentItem();
                if (currentItem == 0) {
                    viewPager.setCurrentItem(1);
                } else if (currentItem == 1) {
                    viewPager.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(0);
                }
            });
        }
    }
}
