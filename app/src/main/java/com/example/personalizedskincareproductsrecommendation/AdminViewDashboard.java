package com.example.personalizedskincareproductsrecommendation;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminViewDashboard extends AppCompatActivity {
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private FirebaseAuth mAuth;
    private ImageView back;
    private TextView totalUsers, oilyUsers, dryUsers;
    private DatabaseReference databaseReference, userReference;


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

        databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(userId);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminViewDashboard.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        totalUsers = findViewById(R.id.num_users);
        oilyUsers = findViewById(R.id.oily_skin_users);
        dryUsers = findViewById(R.id.dry_skin_users);

        userReference = FirebaseDatabase.getInstance().getReference("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int counter = (int) dataSnapshot.getChildrenCount();
                //Convert counter to string
                String userCounter = String.valueOf(counter);

                //Showing the user counter in the textview
                totalUsers.setText(userCounter);
                Log.d("User Counter", userCounter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}