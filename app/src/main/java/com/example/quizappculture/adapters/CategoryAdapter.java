package com.example.quizappculture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizappculture.R;
import com.example.quizappculture.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;
    
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    
    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        
        holder.tvCategoryName.setText(category.getName());
        holder.ivCategoryIcon.setImageResource(category.getIconResId());
        
        if (category.isUnlocked()) {
            holder.cardCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.category_unlocked));
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.category_text_unlocked));
            holder.ivLock.setVisibility(View.GONE);
        } else {
            holder.cardCategory.setCardBackgroundColor(ContextCompat.getColor(context, R.color.category_locked));
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.category_text_locked));
            holder.ivLock.setVisibility(View.VISIBLE);
        }
        
        holder.cardCategory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardCategory;
        ImageView ivCategoryIcon;
        ImageView ivLock;
        TextView tvCategoryName;
        
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCategory = itemView.findViewById(R.id.cardCategory);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivLock = itemView.findViewById(R.id.ivLock);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}