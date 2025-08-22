package com.example.quizappculture.models;

import java.util.HashMap;
import java.util.Map;

public class UserProgress {
    
    private int level;
    private int experience;
    private int experienceToNextLevel;
    private Map<String, Integer> categoryHighScores;
    private Map<String, Boolean> unlockedCategories;
    private Map<String, Boolean> unlockedDifficulties;
    
    // Constantes pour le système de progression
    private static final int BASE_XP_TO_LEVEL = 100;
    private static final float LEVEL_SCALING_FACTOR = 1.5f;
    
    public UserProgress() {
        // Valeurs par défaut
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = calculateXpForNextLevel(level);
        this.categoryHighScores = new HashMap<>();
        this.unlockedCategories = new HashMap<>();
        this.unlockedDifficulties = new HashMap<>();
        
        // Initialiser les catégories et difficultés déverrouillées par défaut
        initializeUnlocks();
    }
    
    private void initializeUnlocks() {
        // Déverrouiller les catégories et difficultés de base
        String[] defaultCategories = {"Général", "Histoire", "Géographie"};
        for (String category : defaultCategories) {
            unlockedCategories.put(category, true);
        }
        
        // Toutes les autres catégories sont verrouillées par défaut
        String[] lockedCategories = {"Science", "Sport", "Musique", "Cinéma"};
        for (String category : lockedCategories) {
            unlockedCategories.put(category, false);
        }
        
        // Seule la difficulté facile est déverrouillée au début
        unlockedDifficulties.put("Facile", true);
        unlockedDifficulties.put("Moyen", false);
        unlockedDifficulties.put("Difficile", false);
    }
    
    public void addExperience(int xp) {
        this.experience += xp;
        
        // Vérifier si le joueur passe au niveau suivant
        while (experience >= experienceToNextLevel) {
            levelUp();
        }
    }
    
    private void levelUp() {
        level++;
        experience -= experienceToNextLevel;
        experienceToNextLevel = calculateXpForNextLevel(level);
        
        // Déverrouiller des contenus en fonction du niveau
        unlockContentBasedOnLevel();
    }
    
    private int calculateXpForNextLevel(int currentLevel) {
        return (int) (BASE_XP_TO_LEVEL * Math.pow(LEVEL_SCALING_FACTOR, currentLevel - 1));
    }
    
    private void unlockContentBasedOnLevel() {
        // Déverrouiller des difficultés en fonction du niveau
        if (level >= 5 && !unlockedDifficulties.getOrDefault("Moyen", false)) {
            unlockedDifficulties.put("Moyen", true);
        }
        
        if (level >= 10 && !unlockedDifficulties.getOrDefault("Difficile", false)) {
            unlockedDifficulties.put("Difficile", true);
        }
        
        // Déverrouiller des catégories en fonction du niveau
        if (level >= 3) {
            unlockedCategories.put("Science", true);
        }
        
        if (level >= 5) {
            unlockedCategories.put("Sport", true);
        }
        
        if (level >= 7) {
            unlockedCategories.put("Musique", true);
        }
        
        if (level >= 9) {
            unlockedCategories.put("Cinéma", true);
        }
    }
    
    public void updateCategoryScore(String category, int score) {
        int currentHighScore = categoryHighScores.getOrDefault(category, 0);
        if (score > currentHighScore) {
            categoryHighScores.put(category, score);
        }
    }
    
    // Getters et setters
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public void setExperience(int experience) {
        this.experience = experience;
    }
    
    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }
    
    public void setExperienceToNextLevel(int experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }
    
    public Map<String, Integer> getCategoryHighScores() {
        return categoryHighScores;
    }
    
    public void setCategoryHighScores(Map<String, Integer> categoryHighScores) {
        this.categoryHighScores = categoryHighScores;
    }
    
    public Map<String, Boolean> getUnlockedCategories() {
        return unlockedCategories;
    }
    
    public void setUnlockedCategories(Map<String, Boolean> unlockedCategories) {
        this.unlockedCategories = unlockedCategories;
    }
    
    public Map<String, Boolean> getUnlockedDifficulties() {
        return unlockedDifficulties;
    }
    
    public void setUnlockedDifficulties(Map<String, Boolean> unlockedDifficulties) {
        this.unlockedDifficulties = unlockedDifficulties;
    }
    
    public boolean isCategoryUnlocked(String category) {
        return unlockedCategories.getOrDefault(category, false);
    }
    
    public boolean isDifficultyUnlocked(String difficulty) {
        return unlockedDifficulties.getOrDefault(difficulty, false);
    }
    
    public int getProgressPercentage() {
        if (experienceToNextLevel == 0) return 100;
        return (experience * 100) / experienceToNextLevel;
    }
}