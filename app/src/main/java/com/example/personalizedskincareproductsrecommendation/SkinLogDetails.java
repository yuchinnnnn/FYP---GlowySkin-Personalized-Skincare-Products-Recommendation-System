package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SkinLogDetails extends AppCompatActivity {

    private String logId, userId;
    private TextView dateTextView, timeTextView;
    private ImageView back, leftSelfie, rightSelfie, frontSelfie, neckSelfie;
    private TextInputEditText notesInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_log_details);

        userId = getIntent().getStringExtra("ARG_USER_ID");
        logId = getIntent().getStringExtra("ARG_LOG_ID");

        // Initialize UI components
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
//            SkinLogHistory skinLogHistory = SkinLogHistory.newInstance(userId);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, skinLogHistory)
//                    .addToBackStack(null)
//                    .commit();
            finish();
        });


        dateTextView = findViewById(R.id.date);
        timeTextView = findViewById(R.id.time);
        leftSelfie = findViewById(R.id.left_selfie);
        rightSelfie = findViewById(R.id.right_selfie);
        frontSelfie = findViewById(R.id.front_selfie);
        neckSelfie = findViewById(R.id.neck_selfie);
        notesInput = findViewById(R.id.notesInputEditText);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the input from the notes field
                String updatedNotes = notesInput.getText().toString().trim();

                if (!updatedNotes.isEmpty()) {
                    // Save notes to Firebase under the respective logId
                    DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("SkinLog").child(userId).child(logId).child("notes");

                    notesRef.setValue(updatedNotes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Notify user that notes were successfully saved
                                Toast.makeText(SkinLogDetails.this, "Notes saved successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle errors
                                Toast.makeText(SkinLogDetails.this, "Failed to save notes", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    SweetAlertDialog alertDialog = new SweetAlertDialog(SkinLogDetails.this, SweetAlertDialog.WARNING_TYPE);
                    alertDialog.setTitleText("Alert");
                    alertDialog.setContentText("You don't have any notes to save. Do you want to add some notes?");
                    alertDialog.setConfirmText("Yes");
                    alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                        }
                    });
                    alertDialog.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            finish();
                        }
                    });
                    alertDialog.show();
                }
            }
        });

        // Fetch and display the log details
        fetchSkinLogDetails(userId, logId);
    }

    private void fetchSkinLogDetails(String userId, String logId) {
        // Reference to Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("SkinLog").child(userId).child(logId);

        // Fetch the log details from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming data structure is like {timestamp, left_selfie, right_selfie, front_selfie, neck_selfie, notes}
                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class);
                    String leftSelfieUrl = dataSnapshot.child("selfies").child("left").getValue(String.class);
                    String rightSelfieUrl = dataSnapshot.child("selfies").child("right").getValue(String.class);
                    String frontSelfieUrl = dataSnapshot.child("selfies").child("front").getValue(String.class);
                    String neckSelfieUrl = dataSnapshot.child("selfies").child("neck").getValue(String.class);

                    String notes = dataSnapshot.child("notes").getValue(String.class);
                    if (notes != null && !notes.isEmpty()) {
                        // Notes exist, allow editing
                        notesInput.setText(notes);
                        notesInput.setEnabled(true);
                    } else {
                        // No notes, prompt the user to write new notes
                        notesInput.setHint("Add your notes here...");
                        notesInput.setEnabled(true);
                    }

                    try {
                        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = originalFormat.parse(timestamp);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Use hh for 12-hour format and add "a" for AM/PM

                        dateTextView.setText("Date: " + dateFormat.format(date));
                        timeTextView.setText("Time: " + timeFormat.format(date));

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle the case where the timestamp is not in the expected format
                        dateTextView.setText("Invalid");
                        timeTextView.setText("Invalid");
                    }


                    // Display selfies using Glide or Picasso
                    if (leftSelfieUrl != null) {
                        Glide.with(SkinLogDetails.this).load(leftSelfieUrl).into(leftSelfie);
                    }
                    if (rightSelfieUrl != null) {
                        Glide.with(SkinLogDetails.this).load(rightSelfieUrl).into(rightSelfie);
                    }
                    if (frontSelfieUrl != null) {
                        Glide.with(SkinLogDetails.this).load(frontSelfieUrl).into(frontSelfie);
                    }
                    if (neckSelfieUrl != null) {
                        Glide.with(SkinLogDetails.this).load(neckSelfieUrl).into(neckSelfie);
                    }


//                    // Display selfies using Glide or Picasso
//                    Glide.with(SkinLogDetails.this).load(leftSelfieUrl).into(leftSelfie);
//                    Glide.with(SkinLogDetails.this).load(rightSelfieUrl).into(rightSelfie);
//                    Glide.with(SkinLogDetails.this).load(frontSelfieUrl).into(frontSelfie);
//                    Glide.with(SkinLogDetails.this).load(neckSelfieUrl).into(neckSelfie);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(SkinLogDetails.this, "Failed to load log details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
