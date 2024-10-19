package com.example.personalizedskincareproductsrecommendation;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

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

        // Initialize your UI components for each list item
        TextView usernameField = convertView.findViewById(R.id.username_field);
        TextView emailField = convertView.findViewById(R.id.email_field);
        TextView deactivateButton = convertView.findViewById(R.id.deactivate);
        TextView reactivateButton = convertView.findViewById(R.id.reactivate);
        ConstraintLayout listLayout = convertView.findViewById(R.id.userProfileLayout);  // Assuming you have this layout in users_profile_list.xml

        // Get the current user from the list
        Users user = userList.get(position);

        // Set the data for the current user
        usernameField.setText("Username: " + user.getUsername());
        emailField.setText("Email: " + user.getEmail());

        // Set the visibility of the deactivate button based on user status
        deactivateButton.setVisibility(user.getStatus().equals("active") ? View.VISIBLE : View.GONE);
        deactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeactivateConfirmationDialog(user);
            }
        });

        reactivateButton.setVisibility(user.getStatus().equals("deactivated") ? View.VISIBLE : View.GONE);
        reactivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReactivateConfirmationDialog(user);
            }
        });

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

    // Method to update the list and notify the adapter of changes
    public void updateList(List<Users> updatedUserList) {
        this.userList = new ArrayList<>(updatedUserList); // Create a new list to prevent referencing issues
        notifyDataSetChanged();  // Refresh the ListView
    }

    private void showReactivateConfirmationDialog(Users user) {
        // Create a layout inflater
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_reactivate_user, null);

        // Get the EditText for the reason input
        EditText editReason = dialogView.findViewById(R.id.edit_reason);

        // Show a confirmation dialog
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle("Reactivate User");
        sweetAlertDialog.setCustomView(dialogView);  // Set the custom view
        sweetAlertDialog.setConfirmText("Yes");

        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            String reason = editReason.getText().toString().trim();
            if (reason.isEmpty()) {
                // Show an error message if reason is empty
                Toast.makeText(context, "Please provide a reason for reactivation.", Toast.LENGTH_SHORT).show();
            } else {
                reactivateUser(user, reason);  // Pass the reason to the deactivateUser method
                sweetAlertDialog1.dismiss();
            }
        });

        sweetAlertDialog.setCancelText("No");
        sweetAlertDialog.setCancelClickListener(sweetAlertDialog1 -> {
            sweetAlertDialog1.dismiss();
        });

        sweetAlertDialog.show();
    }


    private void showDeactivateConfirmationDialog(Users user) {
        // Create a layout inflater
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_deactivate_user, null);

        // Get the EditText for the reason input
        EditText editReason = dialogView.findViewById(R.id.edit_reason);

        // Show a confirmation dialog
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitle("Deactivate User");
        sweetAlertDialog.setCustomView(dialogView);  // Set the custom view
        sweetAlertDialog.setConfirmText("Yes");

        sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
            String reason = editReason.getText().toString().trim();
            if (reason.isEmpty()) {
                // Show an error message if reason is empty
                Toast.makeText(context, "Please provide a reason for deactivation.", Toast.LENGTH_SHORT).show();
            } else {
                deactivateUser(user, reason);  // Pass the reason to the deactivateUser method
                sweetAlertDialog1.dismiss();
            }
        });

        sweetAlertDialog.setCancelText("No");
        sweetAlertDialog.setCancelClickListener(sweetAlertDialog1 -> {
            sweetAlertDialog1.dismiss();
        });

        sweetAlertDialog.show();
    }

    public void filter(String query) {
        List<Users> filteredList = new ArrayList<>();
        for (Users user : userList) {
            if (user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        // Update the adapter with the filtered list
        this.userList = filteredList;
        notifyDataSetChanged();
    }

    public void resetFilter(List<Users> originalList) {
        this.userList = originalList;
        notifyDataSetChanged();
    }


    private void deactivateUser(Users user, String reason) {
        String userId = user.getUserId();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("status").setValue("deactivated").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update user's status locally
                user.setStatus("deactivated");
                notifyDataSetChanged(); // Refresh the list
                Toast.makeText(context, "User deactivated successfully", Toast.LENGTH_SHORT).show();
                Log.d("DeactivateUser", "User " + user.getUsername() + " deactivated. Reason: " + reason);
            } else {
                Toast.makeText(context, "Failed to deactivate user", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void reactivateUser(Users user, String reason) {
        String userId = user.getUserId();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("status").setValue("active").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update user's status locally
                user.setStatus("active");
                notifyDataSetChanged(); // Refresh the list
                Toast.makeText(context, "User reactivated successfully", Toast.LENGTH_SHORT).show();
                Log.d("ReactivateUser", "User " + user.getUsername() + " reactivated. Reason: " + reason);
            } else {
                Toast.makeText(context, "Failed to reactivate user", Toast.LENGTH_SHORT).show();
            }
        });
    }

}