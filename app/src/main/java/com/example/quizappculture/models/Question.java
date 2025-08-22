package com.example.quizappculture.models;

import java.util.List;

public class Question {
    private String question;
    private List<String> options;
    private String correctAnswer;
    private String category;
    private String difficulty;
    private String imageUrl;
    
    // Required empty constructor for Firebase
    public Question() {
    }
    
    public Question(String question, List<String> options, String correctAnswer, String category, String difficulty) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.difficulty = difficulty;
    }
    
    public Question(String question, List<String> options, String correctAnswer, String category, String difficulty, String imageUrl) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.category = category;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getCategory() {
        return category != null ? category : "Général";
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDifficulty() {
        return difficulty != null ? difficulty : "Moyen";
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getPointValue() {
        switch (getDifficulty()) {
            case "Facile":
                return 1;
            case "Moyen":
                return 2;
            case "Difficile":
                return 3;
            default:
                return 1;
        }
    }
}