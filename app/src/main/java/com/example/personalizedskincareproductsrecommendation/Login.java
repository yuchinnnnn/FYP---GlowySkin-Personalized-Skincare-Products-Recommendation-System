package com.example.personalizedskincareproductsrecommendation;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private TextInputLayout loginUsernameLayout, loginPasswordLayout;
    private TextInputEditText loginUsername, loginPassword;
    private TextView signUp, login;
    private FirebaseAuth database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsernameLayout = findViewById(R.id.loginUsernameLayout);
        loginUsername = findViewById(R.id.loginUsername);
        loginPasswordLayout = findViewById(R.id.loginPasswordLayout);
        loginPassword = findViewById(R.id.loginPassword);
        signUp = findViewById(R.id.signUp);
        login = findViewById(R.id.login);

        database = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validateUsername() | !validatePassword()) {

                } else {
                    checkUser();
                }
            }
        });

        // Create a SpannableString with the desired text
        String text = "Don't have an account? Sign Up";

        SpannableString spannableString = new SpannableString(text);

        // Set color for "Don't have an account?"
        spannableString.setSpan(new ForegroundColorSpan(Color.GRAY), 0, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set color for "Sign Up"
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 23, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply the SpannableString to the TextView
        signUp.setText(spannableString);

        // Set onClickListener for the TextView
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
    }


    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();

        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty.");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();

        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty.");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference adminReference = FirebaseDatabase.getInstance().getReference("Admin");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);
        Query checkAdminDatabase = adminReference.orderByChild("username").equalTo(userUsername);

        // Check the Admin database first
        checkAdminDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleLogin(snapshot, userPassword, true);
                } else {
                    // Admin username not found, check the Users database
                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {
                                handleLogin(userSnapshot, userPassword, false);
                            } else {
                                // Username not found in both Admin and Users databases
                                loginUsername.setError("Username not found.");
                                loginUsername.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle possible errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    // Helper method to handle login logic based on database result
    private void handleLogin(DataSnapshot snapshot, String userPassword, boolean isAdmin) {
        boolean credentialsValid = false;
        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
            String userId = userSnapshot.getKey();
            String usernameFromDB = userSnapshot.child("username").getValue(String.class);
            String hashedPasswordFromDB = userSnapshot.child("password").getValue(String.class);

            // Verify password using BCrypt
            if (BCrypt.checkpw(userPassword, hashedPasswordFromDB)) {
                credentialsValid = true;
                loginUsername.setError(null);
                loginPassword.setError(null); // Clear any previous error

                // Proceed to the appropriate activity based on user type
                Intent intent;
                if (isAdmin) {
                    intent = new Intent(Login.this, AdminDashboard.class);
                    Toast.makeText(Login.this, "Welcome, Admin " + usernameFromDB, Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(Login.this, Homepage.class);
                    Toast.makeText(Login.this, "Welcome, " + usernameFromDB, Toast.LENGTH_SHORT).show();
                }
                intent.putExtra("userId", userId); // Pass the user ID to the next screen
                startActivity(intent);
                break;
            }
        }

        if (!credentialsValid) {
            // If username found but password is incorrect
            loginPassword.setError("Invalid Credentials.");
            loginPassword.requestFocus();
        }
    }

}
