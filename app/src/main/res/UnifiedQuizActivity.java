package com.example.quizappculture;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
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

import com.example.quizappculture.models.Question;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UnifiedQuizActivity extends AppCompatActivity {
    
    // UI components
    private TextView tvQuestion, tvQuestionCounter, tvTimer;
    private RadioGroup rgAnswers;
    private RadioButton rbAnswer1, rbAnswer2, rbAnswer3, rbAnswer4;
    private Button btnNext;
    private ProgressBar progressBar;
    
    // Quiz data
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalQuestions = 10;
    
    // Timer
    private static final long QUESTION_TIMEOUT_MS = 30000; // 30 seconds per question
    private CountDownTimer timer;
    private long timeLeftInMs = QUESTION_TIMEOUT_MS;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize UI components
        initializeViews();
        
        // Set up button listeners
        setupButtonListeners();
        
        // Load questions from Firebase
        loadQuestionsFromFirebase();
    }
    
    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvTimer = findViewById(R.id.tvTimer);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbAnswer1 = findViewById(R.id.rbAnswer1);
        rbAnswer2 = findViewById(R.id.rbAnswer2);
        rbAnswer3 = findViewById(R.id.rbAnswer3);
        rbAnswer4 = findViewById(R.id.rbAnswer4);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        
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
    
    private void loadQuestionsFromFirebase() {
        mDatabase.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot questionSnapshot : dataSnapshot.getChildren()) {
                    Question question = questionSnapshot.getValue(Question.class);
                    if (question != null) {
                        questionList.add(question);
                    }
                }
                
                // If we don't have enough questions in Firebase yet, use static questions
                if (questionList.size() < totalQuestions) {
                    addStaticQuestions();
                }
                
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
        String[][] staticQuestions = {
            {"Quelle est la plus grande planète du système solaire?", "Jupiter", "Saturne", "Neptune", "Uranus", "Jupiter"},
            {"Que produit une abeille outre le miel?", "La cire", "La propolis", "Le pollen", "La gelée royale", "La propolis"},
            {"Quelle est la planète la plus proche du soleil?", "Vénus", "Mercure", "Terre", "Mars", "Mercure"},
            {"Qui a peint la Joconde?", "Michel-Ange", "Léonard de Vinci", "Vincent Van Gogh", "Pablo Picasso", "Léonard de Vinci"},
            {"Quelle est la capitale de l'Australie?", "Sydney", "Melbourne", "Canberra", "Brisbane", "Canberra"},
            {"Quel est le plus grand océan du monde?", "Atlantique", "Indien", "Arctique", "Pacifique", "Pacifique"},
            {"Quel est le plus long fleuve du monde?", "Nil", "Amazone", "Mississippi", "Yangtsé", "Nil"},
            {"En quelle année a commencé la Première Guerre mondiale?", "1912", "1914", "1916", "1918", "1914"},
            {"Quel est le pays le plus peuplé du monde?", "Inde", "Chine", "États-Unis", "Indonésie", "Chine"},
            {"Quelle est la plus haute montagne du monde?", "Mont Blanc", "K2", "Everest", "Kilimandjaro", "Everest"}
        };
        
        // Add static questions to question list
        for (String[] qData : staticQuestions) {
            Question q = new Question();
            q.setQuestion(qData[0]);
            List<String> options = new ArrayList<>();
            options.add(qData[1]);
            options.add(qData[2]);
            options.add(qData[3]);
            options.add(qData[4]);
            q.setOptions(options);
            q.setCorrectAnswer(qData[5]);
            questionList.add(q);
        }
    }
    
    private void displayQuestion(int index) {
        if (index < questionList.size()) {
            Question currentQuestion = questionList.get(index);
            
            // Update UI with question data
            tvQuestion.setText(currentQuestion.getQuestion());
            tvQuestionCounter.setText(String.format(Locale.getDefault(), "Question %d/%d", index + 1, totalQuestions));
            
            // Set answer options
            List<String> options = currentQuestion.getOptions();
            if (options != null && options.size() >= 4) {
                rbAnswer1.setText(options.get(0));
                rbAnswer2.setText(options.get(1));
                rbAnswer3.setText(options.get(2));
                rbAnswer4.setText(options.get(3));
            }
            
            // Clear previous selection
            rgAnswers.clearCheck();
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
            
            if (selectedRb != null && selectedRb.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                correctAnswers++;
            }
        }
    }
    
    private void moveToNextQuestion() {
        currentQuestionIndex++;
        
        if (currentQuestionIndex < totalQuestions) {
            displayQuestion(currentQuestionIndex);
            startTimer();
        } else {
            // Quiz completed - calculate score and move to results
            finishQuiz();
        }
    }
    
    private void finishQuiz() {
        if (timer != null) {
            timer.cancel();
        }
        
        // Calculate percentage
        int percentage = (correctAnswers * 100) / totalQuestions;
        
        // Create intent for score activity
        Intent intentScore = new Intent(UnifiedQuizActivity.this, score.class);
        intentScore.putExtra("score", correctAnswers);
        intentScore.putExtra("total", totalQuestions);
        intentScore.putExtra("percentage", percentage);
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