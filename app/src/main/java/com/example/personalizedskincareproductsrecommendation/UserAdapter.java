package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
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

        TextView usernameField = convertView.findViewById(R.id.username_field);
        TextView emailField = convertView.findViewById(R.id.email_field);
//        ImageView deactivate = convertView.findViewById(R.id.deactivate_button);

        Users user = userList.get(position);

        usernameField.setText("Username: " + user.getUsername());
        emailField.setText("Email: " + user.getEmail());

        listLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click event here
                Intent intent = new Intent(context, AdminViewUserProfile.class);
                intent.putExtra(AdminManageProfile.ARG_USER_ID, user.getUserId());
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}

