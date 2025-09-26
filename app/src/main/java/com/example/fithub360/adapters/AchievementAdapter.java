package com.example.fithub360.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;
import com.example.fithub360.models.Achievement;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievements = new ArrayList<>();

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    public void updateAchievement(Achievement achievement) {
        int position = -1;
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).getId().equals(achievement.getId())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImageView;
        private TextView nameTextView;
        private TextView descriptionTextView;
        private LinearProgressIndicator progressIndicator;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.achievementIcon);
            nameTextView = itemView.findViewById(R.id.achievementName);
            descriptionTextView = itemView.findViewById(R.id.achievementDescription);
            progressIndicator = itemView.findViewById(R.id.achievementProgress);
        }

        public void bind(Achievement achievement) {
            nameTextView.setText(achievement.getName());
            descriptionTextView.setText(achievement.getDescription());

            // Configurar el icono según el estado
            iconImageView.setAlpha(achievement.isUnlocked() ? 1.0f : 0.5f);

            // Configurar la barra de progreso
            progressIndicator.setMax(achievement.getRequiredCount());
            progressIndicator.setProgress(achievement.getCurrentProgress());
        }
    }
}
