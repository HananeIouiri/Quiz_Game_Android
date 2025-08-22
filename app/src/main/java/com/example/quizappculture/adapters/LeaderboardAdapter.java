package com.example.quizappculture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizappculture.R;
import com.example.quizappculture.models.Score;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    
    private final Context context;
    private final List<Score> scoreList;
    
    public LeaderboardAdapter(Context context, List<Score> scoreList) {
        this.context = context;
        this.scoreList = scoreList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Score score = scoreList.get(position);
        
        // Set rank (position + 1)
        holder.tvRank.setText(String.valueOf(position + 1));
        
        // Set username
        holder.tvUsername.setText(score.getUsername());
        
        // Set score
        holder.tvScore.setText(String.valueOf(score.getScore()));
        
        // Highlight top 3 ranks
        if (position == 0) {
            // Gold medal (1st place)
            holder.tvRank.setBackgroundResource(R.drawable.rank_gold);
        } else if (position == 1) {
            // Silver medal (2nd place)
            holder.tvRank.setBackgroundResource(R.drawable.rank_silver);
        } else if (position == 2) {
            // Bronze medal (3rd place)
            holder.tvRank.setBackgroundResource(R.drawable.rank_bronze);
        } else {
            // Regular rank
            holder.tvRank.setBackgroundResource(R.drawable.rank_regular);
        }
    }
    
    @Override
    public int getItemCount() {
        return scoreList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvScore;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}