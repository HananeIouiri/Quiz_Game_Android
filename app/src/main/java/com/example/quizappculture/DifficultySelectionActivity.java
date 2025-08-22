package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

public class DifficultySelectionActivity extends AppCompatActivity {
    
    // UI Components
    private TextView tvCategoryName;
    private ImageView ivCategoryIcon;
    private CardView cardEasyDifficulty, cardMediumDifficulty, cardHardDifficulty;
    private ImageView ivMediumLock, ivHardLock;
    private TextView tvMediumUnlockLevel, tvHardUnlockLevel;
    
    // Data
    private String selectedCategory;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private UserProgress userProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_difficulty_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Obtenir la catégorie sélectionnée
        if (getIntent().hasExtra("category")) {
            selectedCategory = getIntent().getStringExtra("category");
        } else {
            selectedCategory = "Général";
        }
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Initialize UI components
        initializeViews();
        
        // Update UI with selected category
        updateCategoryInfo();
        
        // Load user progress
        loadUserProgress();
        
        // Set up listeners
        setupDifficultyListeners();
    }
    
    private void initializeViews() {
        tvCategoryName = findViewById(R.id.tvCategoryName);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        
        cardEasyDifficulty = findViewById(R.id.cardEasyDifficulty);
        cardMediumDifficulty = findViewById(R.id.cardMediumDifficulty);
        cardHardDifficulty = findViewById(R.id.cardHardDifficulty);
        
        ivMediumLock = findViewById(R.id.ivMediumLock);
        ivHardLock = findViewById(R.id.ivHardLock);
        
        tvMediumUnlockLevel = findViewById(R.id.tvMediumUnlockLevel);
        tvHardUnlockLevel = findViewById(R.id.tvHardUnlockLevel);
    }
    
    private void updateCategoryInfo() {
        tvCategoryName.setText(selectedCategory);
        
        // Définir l'icône en fonction de la catégorie
        switch (selectedCategory) {
            case "Histoire":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_history);
                break;
            case "Géographie":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_geography);
                break;
            case "Science":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_science);
                break;
            case "Sport":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_sports);
                break;
            case "Musique":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_music);
                break;
            case "Cinéma":
                ivCategoryIcon.setImageResource(R.drawable.ic_category_movies);
                break;
            default:
                ivCategoryIcon.setImageResource(R.drawable.ic_category_general);
                break;
        }
    }
    
    private void loadUserProgress() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            mDatabase.child("users").child(userId).child("progress").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userProgress = dataSnapshot.getValue(UserProgress.class);
                        updateUIWithProgress();
                    } else {
                        userProgress = new UserProgress();
                        mDatabase.child("users").child(userId).child("progress").setValue(userProgress);
                        updateUIWithProgress();
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DifficultySelectionActivity.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
                    userProgress = new UserProgress();
                    updateUIWithProgress();
                }
            });
        } else {
            // Si utilisateur non connecté, créer un profil temporaire
            userProgress = new UserProgress();
            updateUIWithProgress();
        }
    }
    
    private void updateUIWithProgress() {
        // Difficulté Moyenne
        if (userProgress.isDifficultyUnlocked("Moyen")) {
            ivMediumLock.setVisibility(View.GONE);
            tvMediumUnlockLevel.setVisibility(View.GONE);
            cardMediumDifficulty.setCardBackgroundColor(getResources().getColor(R.color.white));
        } else {
            ivMediumLock.setVisibility(View.VISIBLE);
            tvMediumUnlockLevel.setVisibility(View.VISIBLE);
            cardMediumDifficulty.setCardBackgroundColor(getResources().getColor(R.color.category_locked));
        }
        
        // Difficulté Difficile
        if (userProgress.isDifficultyUnlocked("Difficile")) {
            ivHardLock.setVisibility(View.GONE);
            tvHardUnlockLevel.setVisibility(View.GONE);
            cardHardDifficulty.setCardBackgroundColor(getResources().getColor(R.color.white));
        } else {
            ivHardLock.setVisibility(View.VISIBLE);
            tvHardUnlockLevel.setVisibility(View.VISIBLE);
            cardHardDifficulty.setCardBackgroundColor(getResources().getColor(R.color.category_locked));
        }
    }
    
    private void setupDifficultyListeners() {
        // Add animations to all difficulty cards when they appear
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        new Handler().postDelayed(() -> cardEasyDifficulty.startAnimation(scaleAnimation), 100);
        new Handler().postDelayed(() -> cardMediumDifficulty.startAnimation(scaleAnimation), 200);
        new Handler().postDelayed(() -> cardHardDifficulty.startAnimation(scaleAnimation), 300);
        
        // Difficulté Facile (toujours disponible)
        cardEasyDifficulty.setOnClickListener(v -> {
            cardEasyDifficulty.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            new Handler().postDelayed(() -> startQuiz("Facile"), 300);
        });
        
        // Difficulté Moyenne (déverrouillée au niveau 5)
        cardMediumDifficulty.setOnClickListener(v -> {
            cardMediumDifficulty.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            if (userProgress.isDifficultyUnlocked("Moyen")) {
                new Handler().postDelayed(() -> startQuiz("Moyen"), 300);
            } else {
                Toast.makeText(this, "Cette difficulté sera débloquée au niveau 5", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Difficulté Difficile (déverrouillée au niveau 10)
        cardHardDifficulty.setOnClickListener(v -> {
            cardHardDifficulty.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            if (userProgress.isDifficultyUnlocked("Difficile")) {
                new Handler().postDelayed(() -> startQuiz("Difficile"), 300);
            } else {
                Toast.makeText(this, "Cette difficulté sera débloquée au niveau 10", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void startQuiz(String difficulty) {
        Intent intent = new Intent(DifficultySelectionActivity.this, UnifiedQuizActivity.class);
        intent.putExtra("category", selectedCategory);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
        
        // Add transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}