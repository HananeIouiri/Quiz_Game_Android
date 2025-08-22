package com.example.quizappculture;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizappculture.adapters.LeaderboardAdapter;
import com.example.quizappculture.models.Score;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<Score> scoreList;
    private ProgressBar progressBar;
    private TextView tvNoData;
    private Button btnBack;
    
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);
        btnBack = findViewById(R.id.btnBack);
        
        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scoreList = new ArrayList<>();
        adapter = new LeaderboardAdapter(this, scoreList);
        recyclerView.setAdapter(adapter);
        
        // Set up back button
        btnBack.setOnClickListener(v -> finish());
        
        // Load leaderboard data
        loadLeaderboardData();
    }
    
    private void loadLeaderboardData() {
        showLoading(true);
        
        // Query top 5 scores ordered by score (descending)
        Query leaderboardQuery = mDatabase.child("leaderboard")
                .orderByChild("score")
                .limitToLast(5); // Top 5 scores
        
        leaderboardQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scoreList.clear();
                
                for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                    Score score = scoreSnapshot.getValue(Score.class);
                    if (score != null) {
                        scoreList.add(0, score); // Add at beginning to reverse order (highest first)
                    }
                }
                
                adapter.notifyDataSetChanged();
                showLoading(false);
                
                if (scoreList.isEmpty()) {
                    tvNoData.setVisibility(View.VISIBLE);
                } else {
                    tvNoData.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                showLoading(false);
                tvNoData.setText("Erreur de chargement des données");
                tvNoData.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvNoData.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}