package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.quizappculture.models.Question;
import com.example.quizappculture.models.UserProgress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UnifiedQuizActivity extends AppCompatActivity {
    
    // UI components
    private TextView tvQuestion, tvQuestionCounter, tvTimer, tvCategory, tvDifficulty;
    private RadioGroup rgAnswers;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;
    private Button btnNext;
    private ProgressBar progressBar;
    private ImageView ivQuestionImage;
    
    // Quiz data
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalQuestions = 10;
    private int totalPoints = 0;
    private String selectedCategory = "Général";
    private String selectedDifficulty = "Facile";
    
    // Timer
    private static final long QUESTION_TIMEOUT_MS = 30000; // 30 seconds per question
    private CountDownTimer timer;
    private long timeLeftInMs = QUESTION_TIMEOUT_MS;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private UserProgress userProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unified_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Get selected category and difficulty from intent
        if (getIntent().hasExtra("category")) {
            selectedCategory = getIntent().getStringExtra("category");
        }
        
        if (getIntent().hasExtra("difficulty")) {
            selectedDifficulty = getIntent().getStringExtra("difficulty");
        }
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Initialize UI components
        initializeViews();
        
        // Set up button listeners
        setupButtonListeners();
        
        // Load user progress
        loadUserProgress();
        
        // Load questions from Firebase based on category and difficulty
        loadQuestionsFromFirebase();
    }
    
    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvTimer = findViewById(R.id.tvTimer);
        tvCategory = findViewById(R.id.tvCategory);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbAnswer1 = findViewById(R.id.rbAnswer1);
        rbAnswer2 = findViewById(R.id.rbAnswer2);
        rbAnswer3 = findViewById(R.id.rbAnswer3);
        rbAnswer4 = findViewById(R.id.rbAnswer4);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        
        // Set category and difficulty text
        tvCategory.setText(selectedCategory);
        tvDifficulty.setText(selectedDifficulty);
        
        // Initialize question list
        questionList = new ArrayList<>();
    }
    
    private void setupButtonListeners() {
        btnNext.setOnClickListener(v -> {
            if (rgAnswers.getCheckedRadioButtonId() == -1) {
                Toast.makeText(getApplicationContext(), "Veuillez sélectionner une réponse", Toast.LENGTH_SHORT).show();
            } else {
                checkAnswer();
                moveToNextQuestion();
            }
        });
    }
    
    private void loadUserProgress() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            
            mDatabase.child("users").child(userId).child("progress").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        userProgress = dataSnapshot.getValue(UserProgress.class);
                    } else {
                        userProgress = new UserProgress();
                        mDatabase.child("users").child(userId).child("progress").setValue(userProgress);
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    userProgress = new UserProgress();
                }
            });
        }
    }
    
    private void loadQuestionsFromFirebase() {
        mDatabase.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null) {
                        // Filter by category and difficulty if specified
                        if ((selectedCategory.equals("Général") || question.getCategory().equals(selectedCategory)) && 
                            (selectedDifficulty.equals("Facile") || question.getDifficulty().equals(selectedDifficulty))) {
                            questionList.add(question);
                        }
                    }
                }
                
                // If we don't have enough questions in Firebase yet, use static questions
                if (questionList.size() < totalQuestions) {
                    addStaticQuestions();
                }
                
                // Shuffle questions
                Collections.shuffle(questionList);
                
                // Start the quiz
                totalQuestions = Math.min(totalQuestions, questionList.size());
                displayQuestion(currentQuestionIndex);
                startTimer();
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UnifiedQuizActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                // Use static questions as fallback
                addStaticQuestions();
                totalQuestions = Math.min(totalQuestions, questionList.size());
                displayQuestion(currentQuestionIndex);
                startTimer();
            }
        });
    }
    
    private void addStaticQuestions() {
        // Static questions to use if Firebase data isn't available or insufficient
        List<Question> staticQuestions = new ArrayList<>();
        
        // General Category - Easy
        Question q1 = new Question();
        q1.setQuestion("Quelle est la plus grande planète du système solaire?");
        List<String> options1 = new ArrayList<>();
        options1.add("Jupiter");
        options1.add("Saturne");
        options1.add("Neptune");
        options1.add("Uranus");
        q1.setOptions(options1);
        q1.setCorrectAnswer("Jupiter");
        q1.setCategory("Général");
        q1.setDifficulty("Facile");
        staticQuestions.add(q1);
        
        // History Category - Medium
        Question q2 = new Question();
        q2.setQuestion("En quelle année a commencé la Première Guerre mondiale?");
        List<String> options2 = new ArrayList<>();
        options2.add("1912");
        options2.add("1914");
        options2.add("1916");
        options2.add("1918");
        q2.setOptions(options2);
        q2.setCorrectAnswer("1914");
        q2.setCategory("Histoire");
        q2.setDifficulty("Moyen");
        staticQuestions.add(q2);
        
        // Science Category - Hard
        Question q3 = new Question();
        q3.setQuestion("Quelle particule élémentaire a une charge négative?");
        List<String> options3 = new ArrayList<>();
        options3.add("Proton");
        options3.add("Neutron");
        options3.add("Électron");
        options3.add("Quark");
        q3.setOptions(options3);
        q3.setCorrectAnswer("Électron");
        q3.setCategory("Science");
        q3.setDifficulty("Difficile");
        staticQuestions.add(q3);
        
        // Geography Category - Easy
        Question q4 = new Question();
        q4.setQuestion("Quelle est la capitale de l'Australie?");
        List<String> options4 = new ArrayList<>();
        options4.add("Sydney");
        options4.add("Melbourne");
        options4.add("Canberra");
        options4.add("Brisbane");
        q4.setOptions(options4);
        q4.setCorrectAnswer("Canberra");
        q4.setCategory("Géographie");
        q4.setDifficulty("Facile");
        staticQuestions.add(q4);
        
        // Sports Category - Medium
        Question q5 = new Question();
        q5.setQuestion("Combien de joueurs composent une équipe de volleyball sur le terrain?");
        List<String> options5 = new ArrayList<>();
        options5.add("5");
        options5.add("6");
        options5.add("7");
        options5.add("8");
        q5.setOptions(options5);
        q5.setCorrectAnswer("6");
        q5.setCategory("Sport");
        q5.setDifficulty("Moyen");
        staticQuestions.add(q5);
        
        // Filter and add static questions based on selected category and difficulty
        for (Question q : staticQuestions) {
            if ((selectedCategory.equals("Général") || q.getCategory().equals(selectedCategory)) && 
                (selectedDifficulty.equals("Facile") || q.getDifficulty().equals(selectedDifficulty))) {
                questionList.add(q);
            }
        }
    }
    
    private void displayQuestion(int index) {
        if (index < questionList.size()) {
            Question currentQuestion = questionList.get(index);
            
            // Apply fade-in animation to content
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            
            // Update UI with question data
            tvQuestion.setText(currentQuestion.getQuestion());
            tvQuestion.startAnimation(fadeIn);
            tvQuestionCounter.setText(String.format(Locale.getDefault(), "Question %d/%d", index + 1, totalQuestions));
            
            // Set answer options
            List<String> options = currentQuestion.getOptions();
            if (options != null && options.size() >= 4) {
                rbAnswer1.setText(options.get(0));
                rbAnswer2.setText(options.get(1));
                rbAnswer3.setText(options.get(2));
                rbAnswer4.setText(options.get(3));
                
                // Apply animations with delay to create a cascade effect
                rbAnswer1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                
                // Add slight delay to each subsequent option
                new Handler().postDelayed(() -> {
                    rbAnswer2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                }, 100);
                
                new Handler().postDelayed(() -> {
                    rbAnswer3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                }, 200);
                
                new Handler().postDelayed(() -> {
                    rbAnswer4.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
                }, 300);
            }
            
            // Load question image if available with animation
            if (currentQuestion.getImageUrl() != null && !currentQuestion.getImageUrl().isEmpty()) {
                ivQuestionImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                     .load(currentQuestion.getImageUrl())
                     .into(ivQuestionImage);
                ivQuestionImage.startAnimation(fadeIn);
            } else {
                ivQuestionImage.setVisibility(View.GONE);
            }
            
            // Clear previous selection
            rgAnswers.clearCheck();
            
            // Reset button backgrounds
            rbAnswer1.setBackground(getResources().getDrawable(R.drawable.option_background));
            rbAnswer2.setBackground(getResources().getDrawable(R.drawable.option_background));
            rbAnswer3.setBackground(getResources().getDrawable(R.drawable.option_background));
            rbAnswer4.setBackground(getResources().getDrawable(R.drawable.option_background));
        }
    }
    
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timeLeftInMs = QUESTION_TIMEOUT_MS;
        
        timer = new CountDownTimer(timeLeftInMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMs = millisUntilFinished;
                updateTimerUI();
                
                // Update progress bar
                int progress = (int) ((QUESTION_TIMEOUT_MS - millisUntilFinished) * 100 / QUESTION_TIMEOUT_MS);
                progressBar.setProgress(progress);
            }
            
            @Override
            public void onFinish() {
                timeLeftInMs = 0;
                updateTimerUI();
                progressBar.setProgress(100);
                
                // Time's up - move to next question without scoring
                Toast.makeText(UnifiedQuizActivity.this, "Temps écoulé!", Toast.LENGTH_SHORT).show();
                moveToNextQuestion();
            }
        }.start();
    }
    
    private void updateTimerUI() {
        int seconds = (int) (timeLeftInMs / 1000);
        tvTimer.setText(String.format(Locale.getDefault(), "%02d", seconds));
        
        // Change color based on time remaining
        if (seconds <= 5) {
            tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (seconds <= 10) {
            tvTimer.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            tvTimer.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
    
    private void checkAnswer() {
        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            RadioButton selectedRb = findViewById(rgAnswers.getCheckedRadioButtonId());
            
            if (selectedRb != null) {
                // Disable all radio buttons to prevent changing answer
                rbAnswer1.setEnabled(false);
                rbAnswer2.setEnabled(false);
                rbAnswer3.setEnabled(false);
                rbAnswer4.setEnabled(false);
                
                String selectedAnswer = selectedRb.getText().toString();
                String correctAnswer = currentQuestion.getCorrectAnswer();
                
                // Find which radio button has the correct answer
                RadioButton correctRb = null;
                if (rbAnswer1.getText().toString().equals(correctAnswer)) {
                    correctRb = rbAnswer1;
                } else if (rbAnswer2.getText().toString().equals(correctAnswer)) {
                    correctRb = rbAnswer2;
                } else if (rbAnswer3.getText().toString().equals(correctAnswer)) {
                    correctRb = rbAnswer3;
                } else if (rbAnswer4.getText().toString().equals(correctAnswer)) {
                    correctRb = rbAnswer4;
                }
                
                // Apply appropriate animations
                if (selectedAnswer.equals(correctAnswer)) {
                    correctAnswers++;
                    
                    // Add points based on difficulty
                    int points = currentQuestion.getPointValue();
                    totalPoints += points;
                    
                    // Apply correct answer animation
                    selectedRb.setBackground(getResources().getDrawable(R.drawable.correct_answer_selector));
                    selectedRb.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                    
                    // Show success message with points
                    Toast.makeText(this, "Correct! +" + points + " points", Toast.LENGTH_SHORT).show();
                } else {
                    // Highlight the wrong selection
                    selectedRb.setBackground(getResources().getDrawable(R.drawable.wrong_answer_selector));
                    
                    // Show the correct answer
                    if (correctRb != null) {
                        correctRb.setBackground(getResources().getDrawable(R.drawable.correct_answer_selector));
                        correctRb.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                    }
                    
                    // Show failure message
                    Toast.makeText(this, "Incorrect! La bonne réponse était: " + correctAnswer, Toast.LENGTH_SHORT).show();
                }
                
                // Add a small delay before moving to next question
                btnNext.setEnabled(false);
                new Handler().postDelayed(() -> {
                    btnNext.setEnabled(true);
                    btnNext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce));
                }, 800);
            }
        }
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        // Apply exit animation
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        tvQuestion.startAnimation(fadeOut);
        rgAnswers.startAnimation(fadeOut);
        
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                if (currentQuestionIndex < totalQuestions) {
                    // Re-enable all radio buttons for next question
                    rbAnswer1.setEnabled(true);
                    rbAnswer2.setEnabled(true);
                    rbAnswer3.setEnabled(true);
                    rbAnswer4.setEnabled(true);
                    
                    displayQuestion(currentQuestionIndex);
                    startTimer();
                } else {
                    // Quiz completed - calculate score and move to results
                    finishQuiz();
                }
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
    
    private void finishQuiz() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Calculate percentage
        int percentage = (correctAnswers * 100) / totalQuestions;
        
        // Update user progress if user is logged in
        if (currentUser != null && userProgress != null) {
            // Add experience based on points earned
            userProgress.addExperience(totalPoints);
            
            // Update category high score
            userProgress.updateCategoryScore(selectedCategory, totalPoints);
            
            // Save progress to Firebase
            mDatabase.child("users").child(currentUser.getUid()).child("progress").setValue(userProgress);
            
            // Add to leaderboard if applicable
            if (totalPoints > 0) {
                mDatabase.child("leaderboard").child(currentUser.getUid()).child("username").setValue(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonyme");
                mDatabase.child("leaderboard").child(currentUser.getUid()).child("score").setValue(totalPoints);
                mDatabase.child("leaderboard").child(currentUser.getUid()).child("timestamp").setValue(System.currentTimeMillis());
            }
        }
        
        // Create intent for score activity
        Intent intentScore = new Intent(UnifiedQuizActivity.this, score.class);
        intentScore.putExtra("score", correctAnswers);
        intentScore.putExtra("total", totalQuestions);
        intentScore.putExtra("percentage", percentage);
        intentScore.putExtra("points", totalPoints);
        intentScore.putExtra("category", selectedCategory);
        intentScore.putExtra("difficulty", selectedDifficulty);
        startActivity(intentScore);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}