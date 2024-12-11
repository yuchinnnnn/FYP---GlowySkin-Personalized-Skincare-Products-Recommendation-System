package com.example.personalizedskincareproductsrecommendation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;


public class SignUp extends AppCompatActivity {
    private EditText signupUsername, signupEmail, signupPassword;
    private Button signupButton;
    private TextView login;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference, adminReference;

    private static final String TAG = "SignUp";
    private static final String ADMIN_CODE = "P21013395*";   // Set the admin Code


    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        signupUsername = findViewById(R.id.signupUsername);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupButton = findViewById(R.id.signupButton);
        login = findViewById(R.id.login);
        ImageView showHidePassword = findViewById(R.id.showHidePassword);
        showHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signupPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    // Show password
                    signupPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                    showHidePassword.setImageResource(R.drawable.baseline_remove_red_eye_24); // Change to "eye open" icon
                } else {
                    // Hide password
                    signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showHidePassword.setImageResource(R.drawable.baseline_visibility_off_24); // Change to "eye closed" icon
                }
                // Move cursor to the end
                signupPassword.setSelection(signupPassword.getText().length());
            }
        });


        // Create a SpannableString with the desired text
        String text = "Already an account? Login";

        SpannableString spannableString = new SpannableString(text);

        // Set color for "Don't have an account?"
        spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 18, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set color for "Sign Up"
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 19, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply the SpannableString to the TextView
        login.setText(spannableString);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = mAuth.getUid();
                String username = signupUsername.getText().toString().trim();
                String email = signupEmail.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                if (username.isEmpty() && email.isEmpty() && pass.isEmpty()) {
                    signupUsername.setError("Username cannot be empty.");
                    signupEmail.setError("Email cannot be empty.");
                    signupPassword.setError("Password cannot be empty.");
                    return;
                }

                if (username.isEmpty()) {
                    signupUsername.setError("Username cannot be empty.");
                    return;
                }

                if (email.isEmpty()) {
                    signupEmail.setError("Email cannot be empty.");
                    return;
                }

                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                if (!email.matches(emailPattern)) {
                    signupEmail.setError("Invalid email format.");
                    return;
                }


                if (pass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty.");
                    return;
                }

                if (!isPasswordStrong(pass)) {
                    signupPassword.setError("Password must be at least 8 characters long, include uppercase, " +
                            "lowercase, digit, and special character.");
                    return;
                }

                // Hash the password
                String hashedPassword = hashPassword(pass);

                // Check if the user is registering as an admin by checking if "admin" is in the email
                if (email.contains("admin")) {
                    promptForAdminCode(userId, username, email, hashedPassword);
                } else {
                    // Register as a normal user
                    registerUser(userId, username, email, hashedPassword, "user", "active");
                }
            }
        });

    }

    private boolean isPasswordStrong(String password) {
        // Check minimum length
        if (password.length() < 8) {
            return false;
        }
        // Check for uppercase letters
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // Check for lowercase letters
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // Check for digits
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // Check for special characters
        if (!password.matches(".*[@#$%^&+=].*")) {
            return false;
        }
        return true;
    }


    private void promptForAdminCode(String userId, String username, String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
        builder.setTitle("Admin Registration");
        builder.setMessage("Please enter the admin code:");

        final EditText input = new EditText(SignUp.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String adminCodeInput = input.getText().toString().trim();
                if (adminCodeInput.equals(ADMIN_CODE)) {
                    // Correct admin code entered, register as admin in both "Users" and "Admin" tables
                    registerAdmin(userId, username, email, password);
                } else if (adminCodeInput.isEmpty()) {
                    Toast.makeText(SignUp.this, "Admin code cannot be empty.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    promptForAdminCode(userId, username, email, password);
                } else {
                    // Incorrect admin code, register as normal user in "Users" table
                    Toast.makeText(SignUp.this, "Incorrect admin code. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void registerAdmin(String userId, String username, String email, String hashedPassword) {
        mAuth.createUserWithEmailAndPassword(email, hashedPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        database = FirebaseDatabase.getInstance();
                        adminReference = database.getReference("Admin").child(userId);

                        // Set up the data structure to be saved in Firebase
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("userId", userId);
                        adminData.put("username", username);
                        adminData.put("email", email);
                        adminData.put("password", hashedPassword);

                        adminReference.setValue(adminData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Sign up successful.", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(SignUp.this, Login.class));
                                } else {
                                    Toast.makeText(SignUp.this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Database error: ", task.getException());
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void registerUser(String userId, String username, String email, String hashedPassword, String userType, String status) {
        mAuth.createUserWithEmailAndPassword(email, hashedPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        String userId = firebaseUser.getUid();
                        database = FirebaseDatabase.getInstance();
                        reference = database.getReference("Users").child(userId);

                        // Set up the data structure to be saved in Firebase, including userType
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("userId", userId);
                        userData.put("username", username);
                        userData.put("email", email);
                        userData.put("password", hashedPassword);
                        userData.put("userType", userType);  // Adding userType for future checks
                        userData.put("status", status);

                        // Other default data structures for skinQuiz, skinGoals, etc.
                        userData.put("skinQuiz", new HashMap<String, Object>());
                        userData.put("skinGoals", new HashMap<String, Object>());
                        userData.put("skinAnalysis", new HashMap<String, Object>());
                        userData.put("history", new HashMap<String, Object>());
                        userData.put("products", new HashMap<String, Object>());
                        userData.put("skinLog", new HashMap<String, Object>());

                        // Save user data to Firebase under "Users"
                        reference.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Sign up successful.", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(SignUp.this, Login.class));
                                } else {
                                    Toast.makeText(SignUp.this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Database error: ", task.getException());
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(SignUp.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Sign up failed: ", task.getException());
                }
            }
        });
    }
}

