package com.example.fithub360.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;
import com.example.fithub360.models.Exercise;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private final List<Exercise> exerciseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

    public ExerciseAdapter(List<Exercise> exerciseList) {
        this.exerciseList = exerciseList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.nameTextView.setText(exercise.getName());
        holder.descriptionTextView.setText(exercise.getDescription());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(exercise);
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView descriptionTextView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.exerciseImage);
            nameTextView = itemView.findViewById(R.id.exerciseName);
            descriptionTextView = itemView.findViewById(R.id.exerciseDescription);
        }
    }
}
