package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuizDashboardActivity extends AppCompatActivity {
    
    private TextView tvWelcome, tvHighScore;
    private Button btnStartQuiz, btnLeaderboard, btnLogout;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // If user not logged in, redirect to login
        if (currentUser == null) {
            startActivity(new Intent(QuizDashboardActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        // Initialize UI components
        tvWelcome = findViewById(R.id.tvWelcome);
        tvHighScore = findViewById(R.id.tvHighScore);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Load user data
        loadUserData();
        
        // Set up button listeners
        setupButtonListeners();
    }
    
    private void loadUserData() {
        String userId = currentUser.getUid();
        
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name == null) name = "Joueur";
                    
                    tvWelcome.setText("Bienvenue, " + name + " !");
                    
                    // Get high score if exists
                    if (dataSnapshot.hasChild("highScore")) {
                        Integer highScore = dataSnapshot.child("highScore").getValue(Integer.class);
                        tvHighScore.setText("Meilleur score: " + highScore);
                        tvHighScore.setVisibility(View.VISIBLE);
                    } else {
                        tvHighScore.setVisibility(View.GONE);
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
    
    private void setupButtonListeners() {
        btnStartQuiz.setOnClickListener(v -> {
            startActivity(new Intent(QuizDashboardActivity.this, UnifiedQuizActivity.class));
        });
        
        btnLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(QuizDashboardActivity.this, LeaderboardActivity.class));
        });
        
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(QuizDashboardActivity.this, MainActivity.class));
            finish();
        });
    }
}