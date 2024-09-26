package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ToApproveUser extends Fragment {

    private ListView approveUserList;
    private List<Users> approveUserListData;
    private UserAdapter approveUserAdapter;
    private DatabaseReference userReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Initialize the ListView and adapter
        approveUserList = view.findViewById(R.id.user_list);
        approveUserListData = new ArrayList<>();
        approveUserAdapter = new UserAdapter(getContext(), approveUserListData);
        approveUserList.setAdapter(approveUserAdapter);

        // Firebase reference
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch users to approve
        fetchUsersToApprove();

//        approveUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Users selectedUser = approveUserListData.get(position);
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

        return view;
    }

    private void fetchUsersToApprove() {
        userReference.orderByChild("status").equalTo("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                approveUserListData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                        approveUserListData.add(user);  // Add users pending approval
                }

                // Notify adapter to update the ListView
                approveUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
