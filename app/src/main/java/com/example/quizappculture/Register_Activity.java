package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register_Activity extends AppCompatActivity {
    private EditText etName, etMail, etPassword, etPassword1;
    private Button bRegister;
    private FirebaseAuth myAuth;
    private DatabaseReference mDatabase;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        setContentView(R.layout.activity_register);
        
        // Initialize Firebase
        myAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize views
        etName = findViewById(R.id.etName);
        etMail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);
        etPassword1 = findViewById(R.id.etPassword1);
        bRegister = findViewById(R.id.bRegister);
        
        // Set up login link
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(Register_Activity.this, MainActivity.class));
            finish();
        });
        
        // Register button click listener
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String mail = etMail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String password1 = etPassword1.getText().toString().trim();
                
                // Validate inputs
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Name is required");
                    etName.requestFocus();
                    return;
                }
                
                if (TextUtils.isEmpty(mail)) {
                    etMail.setError("Email is required");
                    etMail.requestFocus();
                    return;
                }
                
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                    return;
                }
                
                if (TextUtils.isEmpty(password1)) {
                    etPassword1.setError("Please confirm your password");
                    etPassword1.requestFocus();
                    return;
                }
                
                if (password.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    etPassword.requestFocus();
                    return;
                }
                
                if (!password.equals(password1)) {
                    etPassword1.setError("Passwords don't match");
                    etPassword1.requestFocus();
                    return;
                }
                
                // Register user
                signUp(name, mail, password);
            }
        });
    }
    
    public void signUp(String name, String mail, String password) {
        myAuth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful, save user data to database
                            FirebaseUser user = myAuth.getCurrentUser();
                            if (user != null) {
                                // Create user data map
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", mail);
                                userData.put("highScore", 0);
                                
                                // Save to database
                                mDatabase.child("users").child(user.getUid()).setValue(userData)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Registration Successful!", Toast.LENGTH_LONG).show();
                                                
                                                // Redirect to Dashboard
                                                startActivity(new Intent(Register_Activity.this, QuizDashboardActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Failed to save user data: " + dbTask.getException().getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            // Registration failed
                            Toast.makeText(getApplicationContext(),
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}