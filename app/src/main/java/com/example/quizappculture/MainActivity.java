package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize views
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editText);
        btnLogin = findViewById(R.id.button1);
        tvRegister = findViewById(R.id.textView);
        
        // Button click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Register_Activity.class)));
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, redirect to quiz activity
            startActivity(new Intent(MainActivity.this, QuizDashboardActivity.class));
            finish();
        }
    }
    
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        
        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Login successful, redirect to dashboard
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, QuizDashboardActivity.class));
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}