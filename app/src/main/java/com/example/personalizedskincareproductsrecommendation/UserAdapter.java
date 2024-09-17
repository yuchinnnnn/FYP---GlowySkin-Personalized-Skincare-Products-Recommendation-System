package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<Users> userList;
    private LayoutInflater inflater;

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
        ImageView editButton = convertView.findViewById(R.id.edit_button);
        ImageView deleteButton = convertView.findViewById(R.id.delete_button);

        Users user = userList.get(position);

        usernameField.setText("Username: " + user.getUsername());
        emailField.setText("Email: " + user.getEmail());

        // Handle click events for edit and delete buttons if needed

        return convertView;
    }
}

