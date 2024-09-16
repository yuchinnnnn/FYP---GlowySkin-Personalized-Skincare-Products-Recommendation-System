package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private TextView currentHour, greeting, edit_goal, skin_goals;
    private ImageView notification;
    private Button skinQuiz, skinLog, skinAnalysis;
    private LinearLayout skinGoalsContainer;
    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference databaseReference;
    private static final String TAG = "HomeFragment";
    private String userId;

    // Factory method to create a new instance of this fragment using the provided userId
    public static HomeFragment newInstance(String userId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        currentHour = view.findViewById(R.id.currentHour);
        greeting = view.findViewById(R.id.greeting);
        fab = view.findViewById(R.id.fab);
        notification = view.findViewById(R.id.notification);
        edit_goal = view.findViewById(R.id.editGoal);
        skin_goals = view.findViewById(R.id.skinGoals);
        skinGoalsContainer = view.findViewById(R.id.goal);
        bottomNavigationView = view.findViewById(R.id.bottombar);

        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.history) {
                selectedFragment = new HistoryFragment();
            } else if (item.getItemId() == R.id.products) {
                selectedFragment = new ProductsFragment();
            } else if (item.getItemId() == R.id.account) {
                selectedFragment = AccountFragment.newInstance(userId);
            }

            if (selectedFragment != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Retrieve user ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString(ARG_USER_ID);
        }

        if (userId != null) {
            // Reference to the SkinGoals table
            DatabaseReference skinGoalsReference = FirebaseDatabase.getInstance().getReference("SkinGoals").child(userId);

            skinGoalsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Retrieve skin goals
                        List<String> skinGoalsList = new ArrayList<>();
                        for (DataSnapshot goalSnapshot : snapshot.child("skinGoals").getChildren()) {
                            String goal = goalSnapshot.getValue(String.class);
                            if (goal != null) {
                                skinGoalsList.add(goal);
                            }
                        }
                        displaySkinGoals(skinGoalsList);
                    } else {
                        // Handle the case where no skin goals are found
                        skin_goals.setText("You haven't selected any skin goals");
                    }

                    // Retrieve username from Users table
                    DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String username = snapshot.child("username").getValue(String.class);
                            if (username != null) {
                                greeting.setText("Hello, " + username);
                            } else {
                                greeting.setText("Hello, User");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Notification.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });


        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SkinAnalysis.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        edit_goal.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SkinGoals.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        skinQuiz = view.findViewById(R.id.skinquizButton);
        skinQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SkinTypeQuiz1.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        skinLog = view.findViewById(R.id.skinlogButton);
        skinLog.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SkinLog.class);
            intent.putExtra(ARG_USER_ID, userId);
            startActivity(intent);
        });

        skinAnalysis = view.findViewById(R.id.skinAnalysisButton);
        skinAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SkinAnalysis.class);
            startActivity(intent);
        });

        // Set greeting message based on the time of day
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            currentHour.setText("Good Morning");
        } else if (hour >= 12 && hour < 17) {
            currentHour.setText("Good Afternoon");
        } else if (hour >= 17 && hour < 21) {
            currentHour.setText("Good Evening");
        } else {
            currentHour.setText("Good Night");
        }

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm != null) {
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
        }
    }

    private void displaySkinGoals(List<String> skinGoalsList) {
        // Join the skin goals with commas
        StringBuilder skinGoalsBuilder = new StringBuilder();
        for (int i = 0; i < skinGoalsList.size(); i++) {
            skinGoalsBuilder.append(skinGoalsList.get(i));
            if (i < skinGoalsList.size() - 1) {
                skinGoalsBuilder.append(", ");
            }
        }
        skin_goals.setText(skinGoalsBuilder.toString());
    }
}