package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AccountFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";
    private static final String TAG = "AccountFragment";

    private ImageView profile_pic, edit_button;
    private ListView skin, reminders;
    private String[] skin_list = {"Skin Goals", "Skin Types", "Do Skin Types Quiz"};
    private String[] reminders_list = {"Daily morning log reminders", "Daily evening log reminders", "Daily night log reminders"};
    private TextView usernameTextView, emailTextView, logout, deleteAccount;
    private Button home, edit;
    private DatabaseReference databaseReference;
    StorageReference storageReference;
    Uri imageUri;
    private String userId;

    // Factory method to create a new instance of this fragment using the provided userId
    public static AccountFragment newInstance(String userId) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        skin = view.findViewById(R.id.list_item);
        reminders = view.findViewById(R.id.list_reminders);
        usernameTextView = view.findViewById(R.id.username);
        emailTextView = view.findViewById(R.id.email);
        home = view.findViewById(R.id.home_button);
        edit_button = view.findViewById(R.id.edit_button);
        logout = view.findViewById(R.id.logout);
        deleteAccount = view.findViewById(R.id.delete_account);

        ArrayAdapter<String> arr = new ArrayAdapter<>(getContext(), R.layout.listview, R.id.label, skin_list);
        skin.setAdapter(arr);

        ArrayAdapter<String> arr2 = new ArrayAdapter<>(getContext(), R.layout.listview, R.id.label, reminders_list);
        reminders.setAdapter(arr2);

        // Set OnItemClickListener for skin ListView
        skin.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            // Handle the click event based on the selected item
            switch (selectedItem) {
                case "Skin Goals":
                    // Handle "Skin Goals" click
                    Intent intent = new Intent(getContext(), SkinGoals.class);
                    intent.putExtra(ARG_USER_ID, userId);
                    startActivity(intent);
                    break;
                case "Skin Types":
                    // Handle "Skin Types" click
                    Intent intent2 = new Intent(getContext(), SkinType.class);
                    intent2.putExtra(ARG_USER_ID, userId);
                    startActivity(intent2);
                    break;
                case "Do Skin Types Quiz":
                    // Handle "Do Skin Types Quiz" click
                    Intent intent3 = new Intent(getContext(), SkinTypeQuiz1.class);
                    intent3.putExtra(ARG_USER_ID, userId);
                    startActivity(intent3);
                    break;
            }
        });

        // Set OnItemClickListener for reminders ListView
        reminders.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            // Handle the click event based on the selected item
            switch (selectedItem) {
                case "Daily morning log reminders":
                    // Handle "Daily morning log reminders" click
                    Intent intent4 = new Intent(getContext(), MorningReminder.class);
                    intent4.putExtra(ARG_USER_ID, userId);
                    startActivity(intent4);
                    break;
                case "Daily evening log reminders":
                    // Handle "Daily evening log reminders" click
                    Intent intent5 = new Intent(getContext(), AfternoonReminder.class);
                    intent5.putExtra(ARG_USER_ID, userId);
                    startActivity(intent5);
                    break;
                case "Daily night log reminders":
                    // Handle "Daily night log reminders" click
                    Intent intent6 = new Intent(getContext(), NightReminder.class);
                    intent6.putExtra(ARG_USER_ID, userId);
                    startActivity(intent6);
                    break;
            }
        });

        // Retrieve user ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString(ARG_USER_ID);
        }

        if (userId != null) {
            Log.d(TAG, "User ID: " + userId);
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        if (username != null) {
                            usernameTextView.setText(username);
                            Log.d(TAG, "Username: " + username);
                        } else {
                            Log.d(TAG, "Username not found");
                        }

                        if (email != null) {
                            emailTextView.setText(email);
                            Log.d(TAG, "Email: " + email);
                        } else {
                            Log.d(TAG, "Email not found");
                        }
                    } else {
                        Log.d(TAG, "Snapshot does not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });

//            storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userId);
            profile_pic = view.findViewById(R.id.profile_pic);
            loadProfilePhoto(userId);
        } else {
            Log.d(TAG, "User ID is null");
        }

        home.setOnClickListener(v -> {
            HomeFragment homeFragment = HomeFragment.newInstance(userId);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeFragment)
                    .commit();
        });

        edit_button.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            intent.putExtra("userId", userId);  // Pass the userId to the activity
            startActivity(intent);
        });

        // Set underlined text for logout and delete account TextViews
        underlineText(logout, "Logout");
        underlineText(deleteAccount, "Delete Account");

        // Set click listener for logout to navigate to login activity
        logout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        });

        // Set click listener for delete account to show a confirmation dialog
        deleteAccount.setOnClickListener(v -> {
            // Show a confirmation dialog to delete account
        });

        return view;
    }

    private void underlineText(TextView textView, String text) {
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
    }

    private void loadProfilePhoto(String userId) {
        // Construct the path to the user's profile photo URL
        DatabaseReference profileImageRef = databaseReference.child("profilePhotoUrl");

        profileImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the profilePhotoUrl exists
                if (snapshot.exists()) {
                    String profilePhotoUrl = snapshot.getValue(String.class);
                    if (profilePhotoUrl != null) {
                        // Load the image using the URL with Glide
                        Glide.with(getContext()).load(profilePhotoUrl).into(profile_pic);
                        Log.d(TAG, "Profile photo loaded successfully.");
                    } else {
                        Log.d(TAG, "Profile photo URL is null.");
                        // Load a default image if profile photo URL is null
                        profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                    }
                } else {
                    Log.d(TAG, "No profile photo URL found.");
                    // Load a default image if the URL doesn't exist
                    profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile photo URL: " + error.getMessage());
                // Load a default image in case of error
                profile_pic.setImageResource(R.drawable.baseline_account_circle_24); // Set your default image here
            }
        });
    }
}
