package com.example.quizappculture.models;

public class Score {
    private String userId;
    private String username;
    private int score;
    private long timestamp;
    
    // Required empty constructor for Firebase
    public Score() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Score(String userId, String username, int score) {
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}