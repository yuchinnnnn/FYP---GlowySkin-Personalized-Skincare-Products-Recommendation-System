package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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
    private ListView userList;
    private List<Users> userListData;
    private UserAdapter userAdapter;
    private ImageView back;

    private DatabaseReference databaseReference, userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_profile);

        mAuth = FirebaseAuth.getInstance();
        userId = getIntent().getStringExtra(ARG_USER_ID);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);

        // back to admin dashboard
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminManageProfile.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        userList = findViewById(R.id.user_list);

        // Initialize the list and adapter
        userListData = new ArrayList<>();
        userAdapter = new UserAdapter(this, userListData);
        userList.setAdapter(userAdapter);

        // Reference to Firebase "Users" node
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch the list of users from Firebase
        fetchUsers();
    }

    private void fetchUsers() {
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userListData.clear();  // Clear the old list

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    userListData.add(user);  // Add user to the list
                }

                // Notify adapter to update the ListView
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminManageProfile.this, "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
