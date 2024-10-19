package com.example.personalizedskincareproductsrecommendation;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeactivatedUser extends Fragment {

    private ListView deactivatedUserList;
    private List<Users> deactivatedUserListData;
    private UserAdapter deactivatedUserAdapter;
    private DatabaseReference userReference;
    private AutoCompleteTextView search;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        deactivatedUserList = rootView.findViewById(R.id.user_list);
        deactivatedUserListData = new ArrayList<>();
        deactivatedUserAdapter = new UserAdapter(getContext(), deactivatedUserListData);
        deactivatedUserList.setAdapter(deactivatedUserAdapter);

        search = rootView.findViewById(R.id.hint_text);
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

        userReference = FirebaseDatabase.getInstance().getReference("Users");

        fetchDeactivatedUsers();

//        deactivatedUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Users selectedUser = deactivatedUserListData.get(position);
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

    private void fetchDeactivatedUsers() {
        userReference.orderByChild("status").equalTo("deactivated").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deactivatedUserListData.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users user = snapshot.getValue(Users.class);
                    deactivatedUserListData.add(user);
                }

                deactivatedUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load deactivated users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterContent(String searchText) {
        List<Users> filteredList = new ArrayList<>();

        for (Users user : deactivatedUserListData) {
            // Assuming Users has a getUsername() method to get the username
            if (user.getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Update the adapter with the filtered list
        deactivatedUserAdapter.updateList(filteredList);
    }
}