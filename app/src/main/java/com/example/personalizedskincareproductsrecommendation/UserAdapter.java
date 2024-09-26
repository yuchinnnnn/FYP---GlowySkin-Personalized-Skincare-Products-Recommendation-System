package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<Users> userList;
    private LayoutInflater inflater;
    private ConstraintLayout listLayout;

    public UserAdapter(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.users_profile_list, parent, false);
        }

        // Initialize your UI components for each list item
        TextView usernameField = convertView.findViewById(R.id.username_field);
        TextView emailField = convertView.findViewById(R.id.email_field);
        ConstraintLayout listLayout = convertView.findViewById(R.id.userProfileLayout);  // Assuming you have this layout in users_profile_list.xml

        // Get the current user from the list
        Users user = userList.get(position);

        // Set the data for the current user
        usernameField.setText("Username: " + user.getUsername());
        emailField.setText("Email: " + user.getEmail());

        // Set an onClickListener for the user item
        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the AdminViewUserProfile activity
                Intent intent = new Intent(context, AdminViewUserProfile.class);
                // Pass the user ID
                String userId = user.getUserId();
                Log.d("UserAdapter", "Navigating to profile with userId: " + userId);
                intent.putExtra(AdminViewUserProfile.ARG_USER_ID, userId);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}

