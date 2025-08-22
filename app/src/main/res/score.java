package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class score extends AppCompatActivity {
    private TextView tvPercentage, tvScore, tvMessage;
    private ProgressBar progressCircle;
    private Button btnDashboard, btnViewLeaderboard;
    private int score;
    private int total;
    private int percentage;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_score);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        tvPercentage = findViewById(R.id.tv_percentage);
        tvScore = findViewById(R.id.tvScore);
        tvMessage = findViewById(R.id.tvMessage);
        progressCircle = findViewById(R.id.progress);
        btnDashboard = findViewById(R.id.btnDashboard);
        btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard);

        // Get score from intent
        score = getIntent().getIntExtra("score", 0);
        total = getIntent().getIntExtra("total", 10); // Default to 10 if not provided
        percentage = getIntent().getIntExtra("percentage", 0);

        // Update UI
        tvPercentage.setText(percentage + "%");
        tvScore.setText("You answered " + score + " out of " + total + " questions correctly!");
        progressCircle.setProgress(percentage);
        
        // Set message based on score
        setScoreMessage(percentage);

        // Button click listeners
        btnDashboard.setOnClickListener(v -> {
            startActivity(new Intent(score.this, QuizDashboardActivity.class));
            finish();
        });
        
        btnViewLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(score.this, LeaderboardActivity.class));
        });
        
        // Save score to Firebase
        saveScoreToFirebase();
    }
    
    private void setScoreMessage(int percentage) {
        if (percentage >= 80) {
            tvMessage.setText("Excellent! You're a quiz champion!");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (percentage >= 60) {
            tvMessage.setText("Great job! You know your stuff!");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (percentage >= 40) {
            tvMessage.setText("Good effort! Keep learning!");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            tvMessage.setText("Keep practicing! You'll improve!");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
    
    private void saveScoreToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        // Get user data
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name == null) name = "Anonymous";
                    
                    // Create score object
                    com.example.quizappculture.models.Score scoreObj = 
                            new com.example.quizappculture.models.Score(userId, name, score);
                    
                    // Save to leaderboard
                    mDatabase.child("leaderboard").child(userId).setValue(scoreObj);
                    
                    // Update user's high score if this score is higher
                    if (dataSnapshot.hasChild("highScore")) {
                        int highScore = dataSnapshot.child("highScore").getValue(Integer.class);
                        if (score > highScore) {
                            mDatabase.child("users").child(userId).child("highScore").setValue(score);
                        }
                    } else {
                        mDatabase.child("users").child(userId).child("highScore").setValue(score);
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(score.this, "Failed to save score", Toast.LENGTH_SHORT).show();
            }
        });
    }
}