package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ActiveUsers extends Fragment {

    private ListView activeUserList;
    private List<Users> activeUserListData;
    private UserAdapter activeUserAdapter;
    private DatabaseReference userReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        activeUserList = rootView.findViewById(R.id.user_list);
        activeUserListData = new ArrayList<>();
        activeUserAdapter = new UserAdapter(getContext(), activeUserListData);
        activeUserList.setAdapter(activeUserAdapter);

        userReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchActiveUsers();

        // Set item click listener to open the user profile on click
//        activeUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Users selectedUser = activeUserListData.get(position);
//
//                // Start the AdminViewUserProfile activity and pass user data via Intent
//                Intent intent = new Intent(getContext(), AdminViewUserProfile.class);
//                intent.putExtra("username", selectedUser.getUsername());
//                intent.putExtra("email", selectedUser.getEmail());
//                intent.putExtra("age", selectedUser.getAge());
//                intent.putExtra("status", selectedUser.getStatus());
//                intent.putExtra("skinQuizResults", selectedUser.getSkinQuizResults());
//
//                startActivity(intent);
//            }
//        });

        return rootView;
    }

    private void fetchActiveUsers() {
        userReference.orderByChild("status").equalTo("active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activeUserListData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    activeUserListData.add(user);
                }

                activeUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load active users.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
