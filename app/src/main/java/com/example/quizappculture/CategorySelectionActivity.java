package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.Locale;

public class CategorySelectionActivity extends AppCompatActivity {
    
    // UI Components
    private TextView tvLevel, tvXP;
    private ProgressBar progressXP;
    private CardView cardGeneral, cardHistory, cardGeography, cardScience, cardSport;
    private TextView tvGeneralHighScore, tvHistoryHighScore, tvGeographyHighScore;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private UserProgress userProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_selection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Initialize UI components
        initializeViews();
        
        // Set up listeners
        setupCategoryListeners();
        
        // Load user progress
        loadUserProgress();
    }
    
    private void initializeViews() {
        tvLevel = findViewById(R.id.tvLevel);
        tvXP = findViewById(R.id.tvXP);
        progressXP = findViewById(R.id.progressXP);
        
        cardGeneral = findViewById(R.id.cardGeneral);
        cardHistory = findViewById(R.id.cardHistory);
        cardGeography = findViewById(R.id.cardGeography);
        cardScience = findViewById(R.id.cardScience);
        cardSport = findViewById(R.id.cardSport);
        
        tvGeneralHighScore = findViewById(R.id.tvGeneralHighScore);
        tvHistoryHighScore = findViewById(R.id.tvHistoryHighScore);
        tvGeographyHighScore = findViewById(R.id.tvGeographyHighScore);
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
                    Toast.makeText(CategorySelectionActivity.this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
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
        // Mettre à jour les informations de niveau et XP
        tvLevel.setText(String.valueOf(userProgress.getLevel()));
        tvXP.setText(String.format(Locale.getDefault(), "%d/%d", 
                userProgress.getExperience(), userProgress.getExperienceToNextLevel()));
        progressXP.setProgress(userProgress.getProgressPercentage());
        
        // Mettre à jour les meilleurs scores par catégorie
        tvGeneralHighScore.setText(String.format(Locale.getDefault(), 
                "Meilleur score: %d", userProgress.getCategoryHighScores().getOrDefault("Général", 0)));
        tvHistoryHighScore.setText(String.format(Locale.getDefault(), 
                "Meilleur score: %d", userProgress.getCategoryHighScores().getOrDefault("Histoire", 0)));
        tvGeographyHighScore.setText(String.format(Locale.getDefault(), 
                "Meilleur score: %d", userProgress.getCategoryHighScores().getOrDefault("Géographie", 0)));
        
        // Mettre à jour l'état des catégories (verrouillées/déverrouillées)
        updateCategoryLockStatus();
    }

    private void updateCategoryLockStatus() {
        // Science
        // Science - Méthode simplifiée qui change uniquement la couleur d'arrière-plan
        if (userProgress.isCategoryUnlocked("Science")) {
            cardScience.setCardBackgroundColor(getResources().getColor(R.color.category_unlocked));
            // Mettez à jour l'apparence de la carte Science sans accéder à scienceContent/scienceLocked
            // Nous utiliserons directement les enfants de la carte
            ImageView scienceIcon = (ImageView) ((ViewGroup) cardScience.getChildAt(0)).getChildAt(0);
            TextView scienceTitle = (TextView) ((ViewGroup) cardScience.getChildAt(0)).getChildAt(1);
            TextView scienceDescription = (TextView) ((ViewGroup) cardScience.getChildAt(0)).getChildAt(2);

            scienceIcon.setImageResource(R.drawable.ic_category_science);
            scienceTitle.setTextColor(getResources().getColor(R.color.category_text_unlocked));
            scienceDescription.setText("Meilleur score: " + userProgress.getCategoryHighScores().getOrDefault("Science", 0));
            scienceDescription.setTextColor(getResources().getColor(R.color.secondary_text_color));
        } else {
            cardScience.setCardBackgroundColor(getResources().getColor(R.color.category_locked));
            // Pas besoin de changer autre chose car l'apparence par défaut est déjà pour l'état verrouillé
        }

        // Sport
        // Sport - Méthode simplifiée qui change uniquement la couleur d'arrière-plan
        if (userProgress.isCategoryUnlocked("Sport")) {
            cardSport.setCardBackgroundColor(getResources().getColor(R.color.category_unlocked));
            // Mettez à jour l'apparence de la carte Sport sans accéder à sportContent/sportLocked
            ImageView sportIcon = (ImageView) ((ViewGroup) cardSport.getChildAt(0)).getChildAt(0);
            TextView sportTitle = (TextView) ((ViewGroup) cardSport.getChildAt(0)).getChildAt(1);
            TextView sportDescription = (TextView) ((ViewGroup) cardSport.getChildAt(0)).getChildAt(2);

            sportIcon.setImageResource(R.drawable.ic_category_sports);
            sportTitle.setTextColor(getResources().getColor(R.color.category_text_unlocked));
            sportDescription.setText("Meilleur score: " + userProgress.getCategoryHighScores().getOrDefault("Sport", 0));
            sportDescription.setTextColor(getResources().getColor(R.color.secondary_text_color));
        } else {
            cardSport.setCardBackgroundColor(getResources().getColor(R.color.category_locked));
            // Pas besoin de changer autre chose car l'apparence par défaut est déjà pour l'état verrouillé
        }
    }


    private void setupCategoryListeners() {
        // Add animations to all category cards when they appear
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        new Handler().postDelayed(() -> cardGeneral.startAnimation(bounceAnimation), 100);
        new Handler().postDelayed(() -> cardHistory.startAnimation(bounceAnimation), 200);
        new Handler().postDelayed(() -> cardGeography.startAnimation(bounceAnimation), 300);
        new Handler().postDelayed(() -> cardScience.startAnimation(bounceAnimation), 400);
        new Handler().postDelayed(() -> cardSport.startAnimation(bounceAnimation), 500);
        
        // Général
        cardGeneral.setOnClickListener(v -> {
            cardGeneral.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            new Handler().postDelayed(() -> navigateToDifficultySelection("Général"), 300);
        });
        
        // Histoire
        cardHistory.setOnClickListener(v -> {
            cardHistory.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            new Handler().postDelayed(() -> navigateToDifficultySelection("Histoire"), 300);
        });
        
        // Géographie
        cardGeography.setOnClickListener(v -> {
            cardGeography.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
            new Handler().postDelayed(() -> navigateToDifficultySelection("Géographie"), 300);
        });
        
        // Science
        cardScience.setOnClickListener(v -> {
            if (userProgress.isCategoryUnlocked("Science")) {
                cardScience.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                new Handler().postDelayed(() -> navigateToDifficultySelection("Science"), 300);
            } else {
                cardScience.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                Toast.makeText(this, "Cette catégorie sera débloquée au niveau 3", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Sport
        cardSport.setOnClickListener(v -> {
            if (userProgress.isCategoryUnlocked("Sport")) {
                cardSport.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                new Handler().postDelayed(() -> navigateToDifficultySelection("Sport"), 300);
            } else {
                cardSport.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                Toast.makeText(this, "Cette catégorie sera débloquée au niveau 5", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToDifficultySelection(String category) {
        Intent intent = new Intent(CategorySelectionActivity.this, DifficultySelectionActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
        
        // Add transition animation
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}