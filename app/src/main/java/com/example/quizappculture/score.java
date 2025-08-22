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

import com.example.quizappculture.models.UserProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class score extends AppCompatActivity {
    private TextView tvPercentage, tvScore, tvMessage, tvPoints, tvCategory, tvDifficulty;
    private TextView tvLevelUp, tvExpGained;
    private ProgressBar progressCircle;
    private Button btnDashboard, btnViewLeaderboard, btnPlayAgain;
    private int score;
    private int total;
    private int percentage;
    private int points;
    private String category;
    private String difficulty;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private UserProgress userProgress;
    private boolean leveledUp = false;

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
        tvPoints = findViewById(R.id.tvPoints);
        tvCategory = findViewById(R.id.tvCategory);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvLevelUp = findViewById(R.id.tvLevelUp);
        tvExpGained = findViewById(R.id.tvExpGained);
        progressCircle = findViewById(R.id.progress);
        btnDashboard = findViewById(R.id.btnDashboard);
        btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Get data from intent
        score = getIntent().getIntExtra("score", 0);
        total = getIntent().getIntExtra("total", 10); // Default to 10 if not provided
        percentage = getIntent().getIntExtra("percentage", 0);
        points = getIntent().getIntExtra("points", 0);
        category = getIntent().getStringExtra("category");
        difficulty = getIntent().getStringExtra("difficulty");
        
        if (category == null) category = "Général";
        if (difficulty == null) difficulty = "Facile";

        // Update UI
        tvPercentage.setText(percentage + "%");
        tvScore.setText(String.format(Locale.getDefault(), "Vous avez répondu correctement à %d questions sur %d !", score, total));
        tvPoints.setText(String.format(Locale.getDefault(), "Points gagnés: %d", points));
        tvCategory.setText(String.format(Locale.getDefault(), "Catégorie: %s", category));
        tvDifficulty.setText(String.format(Locale.getDefault(), "Difficulté: %s", difficulty));
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
        
        btnPlayAgain.setOnClickListener(v -> {
            Intent intent = new Intent(score.this, CategorySelectionActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Load user progress and save score
        loadUserProgressAndSaveScore();
    }
    
    private void setScoreMessage(int percentage) {
        if (percentage >= 80) {
            tvMessage.setText("Excellent ! Vous êtes un champion du quiz !");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (percentage >= 60) {
            tvMessage.setText("Bravo ! Vous avez de bonnes connaissances !");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        } else if (percentage >= 40) {
            tvMessage.setText("Bon effort ! Continuez à apprendre !");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            tvMessage.setText("Continuez à vous entraîner ! Vous allez vous améliorer !");
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
    
    private void loadUserProgressAndSaveScore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Si l'utilisateur n'est pas connecté, masquer l'info de niveau
            tvLevelUp.setVisibility(View.GONE);
            tvExpGained.setVisibility(View.GONE);
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Get user data and progress
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get username for leaderboard
                    String name = dataSnapshot.child("name").getValue(String.class);
                    if (name == null) name = "Anonyme";
                    
                    // Get the user's progress
                    UserProgress oldProgress = null;
                    if (dataSnapshot.child("progress").exists()) {
                        oldProgress = dataSnapshot.child("progress").getValue(UserProgress.class);
                    }
                    
                    // If there is existing progress, update it
                    if (oldProgress != null) {
                        int oldLevel = oldProgress.getLevel();
                        userProgress = oldProgress;
                        
                        // Add points as experience
                        userProgress.addExperience(points);
                        
                        // Check if user leveled up
                        if (userProgress.getLevel() > oldLevel) {
                            leveledUp = true;
                            tvLevelUp.setVisibility(View.VISIBLE);
                            tvLevelUp.setText(String.format(Locale.getDefault(), 
                                    "Félicitations ! Vous avez atteint le niveau %d !", userProgress.getLevel()));
                        } else {
                            tvLevelUp.setVisibility(View.GONE);
                        }
                        
                        // Update category high score
                        userProgress.updateCategoryScore(category, points);
                        
                        // Show XP gained
                        tvExpGained.setVisibility(View.VISIBLE);
                        tvExpGained.setText(String.format(Locale.getDefault(), 
                                "XP gagnée: %d | Total: %d/%d", 
                                points, userProgress.getExperience(), userProgress.getExperienceToNextLevel()));
                        
                        // Save updated progress
                        mDatabase.child("users").child(userId).child("progress").setValue(userProgress);
                    } else {
                        // Create new progress
                        userProgress = new UserProgress();
                        userProgress.addExperience(points);
                        userProgress.updateCategoryScore(category, points);
                        
                        tvExpGained.setVisibility(View.VISIBLE);
                        tvExpGained.setText(String.format(Locale.getDefault(), 
                                "XP gagnée: %d | Total: %d/%d", 
                                points, userProgress.getExperience(), userProgress.getExperienceToNextLevel()));
                        
                        // Save new progress
                        mDatabase.child("users").child(userId).child("progress").setValue(userProgress);
                    }
                    
                    // Update leaderboard if user scored points
                    if (points > 0) {
                        // Create timestamp for sorting
                        long timestamp = System.currentTimeMillis();
                        
                        // Add to leaderboard with timestamp
                        mDatabase.child("leaderboard").child(userId).child("username").setValue(name);
                        mDatabase.child("leaderboard").child(userId).child("score").setValue(points);
                        mDatabase.child("leaderboard").child(userId).child("timestamp").setValue(timestamp);
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(score.this, "Erreur lors de la sauvegarde du score", Toast.LENGTH_SHORT).show();
            }
        });
    }
}