package com.example.quizappculture.models;

public class Category {
    private String name;
    private int iconResId;
    private boolean unlocked;
    
    public Category() {
        // Required empty constructor for Firebase
    }
    
    public Category(String name, int iconResId, boolean unlocked) {
        this.name = name;
        this.iconResId = iconResId;
        this.unlocked = unlocked;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getIconResId() {
        return iconResId;
    }
    
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}