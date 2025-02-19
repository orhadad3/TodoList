package com.example.todolistproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDescription, textViewDate, textViewUrgency, textViewStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewUrgency = itemView.findViewById(R.id.textViewUrgency);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.textViewDescription.setText(task.getDescription());
        holder.textViewDate.setText("Date: " + task.getDate());
        holder.textViewUrgency.setText("Urgency: " + getUrgencyString(task.getUrgency()));
        holder.textViewStatus.setText("Status: " + getStatusString(task.getStatus()));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private String getUrgencyString(int urgency) {
        switch (urgency) {
            case 1: return "Urgent";
            case 2: return "Very Urgent";
            default: return "Normal";
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case 1: return "Completed";
            case 2: return "Not Completed";
            default: return "Pending";
        }
    }
}