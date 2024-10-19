package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;

public class AdminManageContent extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String userId;
    public static final String ARG_USER_ID = "userId";
    private ImageView back;
    private ImageButton add;
    private RecyclerView recyclerView;
    private ContentAdapter contentAdapter;
    private List<Content> contentList;
    private List<Content> filteredContentList;
    private AutoCompleteTextView search;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_content);

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
                Intent intent = new Intent(AdminManageContent.this, AdminDashboard.class);
                intent.putExtra(AdminDashboard.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        add = findViewById(R.id.add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminManageContent.this, AdminAddContent.class);
                intent.putExtra(AdminAddContent.ARG_USER_ID, userId);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.content_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contentList = new ArrayList<>();
        filteredContentList = new ArrayList<>();
        contentAdapter = new ContentAdapter(this, filteredContentList);
        recyclerView.setAdapter(contentAdapter);

        search = findViewById(R.id.hint_text);

        // Add TextWatcher to the search field
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterContent(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("SkincareTips");

        // Fetch content list from Firebase
        fetchContentList();
    }

    private void fetchContentList() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contentList.clear();
                filteredContentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Content content = snapshot.getValue(Content.class);

                    if (content != null) {
                        // Fetch the coverImage value
                        String coverImage = snapshot.child("coverImage").getValue(String.class);

                        // Set the coverImage if available
                        if (coverImage != null) {
                            content.setCoverImage(coverImage);
                        }

                        // Add the content object to the list
                        contentList.add(content);
                    }
                }

                // Initially show all content
                filteredContentList.addAll(contentList);
                contentAdapter.notifyDataSetChanged();  // Notify adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data", databaseError.toException());
            }
        });
    }


    // Filter content based on search query
    private void filterContent(String query) {
        filteredContentList.clear();

        if (query.isEmpty()) {
            // If search query is empty, show all content
            filteredContentList.addAll(contentList);
        } else {
            // Otherwise, filter content based on the title
            for (Content content : contentList) {
                if (content.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredContentList.add(content);
                }
            }
        }

        contentAdapter.notifyDataSetChanged();  // Notify adapter to refresh the list
    }

}