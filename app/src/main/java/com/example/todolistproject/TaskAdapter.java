package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> taskList;
    private final OnTaskActionListener actionListener;

    private final TaskDao taskDao;

    // Interface for handling task actions (edit, delete)
    public interface OnTaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener actionListener, Context context) {
        this.taskList = taskList;
        this.actionListener = actionListener;
        taskDao = new TaskDao(context);
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textViewDescription.setText(task.getDescription());
        holder.textViewDate.setText("Date: " + task.getDate());
        holder.textViewUrgency.setText("Urgency: " + getUrgencyString(task.getUrgency()));
        holder.textViewStatus.setText("Status: " + getStatusString(task.getStatus()));

        switch (task.getUrgency()) {
            case 1:
                holder.textViewUrgency.setTextColor(Color.RED);
                break;
            case 2:
                holder.textViewUrgency.setTextColor(Color.parseColor("#FF8C00"));
                break;
            default:
                holder.textViewUrgency.setTextColor(Color.GREEN);
        }

        switch (task.getStatus()) {
            case 1:
                holder.textViewStatus.setTextColor(Color.GREEN);
                break;
            case 2:
                holder.textViewStatus.setTextColor(Color.RED);
                break;
            default:
                holder.textViewStatus.setTextColor(Color.GRAY);
        }

        holder.itemView.setOnLongClickListener(v -> {
            showTaskOptionsDialog(holder.itemView, task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private String getUrgencyString(int urgency) {
        switch (urgency) {
            case 1:
                return "Urgent";
            case 2:
                return "Very Urgent";
            default:
                return "Normal";
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case 1:
                return "Completed";
            case 2:
                return "Not Completed";
            default:
                return "Pending";
        }
    }

    private void showTaskOptionsDialog(View view, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Task Options")
                .setItems(new CharSequence[]{"Edit", "Postpone", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        actionListener.onEditTask(task);
                    } else if (which == 1) {
                        showPostponeDialog(view, task);
                    } else if (which == 2) {
                        confirmDeleteTask(view, task);
                    }
                }).show();
    }

    private void showPostponeDialog(View view, Task task) {
        try {
            final Calendar calendar = Calendar.getInstance();

            String currentTaskDate = task.getDate();
            String[] dateParts = currentTaskDate.split("/");

            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);

                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
            }

            @SuppressLint("NotifyDataSetChanged") DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                    (datePicker, year, month, dayOfMonth) -> {
                        @SuppressLint("DefaultLocale") String newDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        task.setDate(newDate);
                        TaskDao taskDao = new TaskDao(view.getContext());
                        taskDao.updateTask(task);
                        notifyDataSetChanged();
                        Toast.makeText(view.getContext(), "Task postponed to " + newDate, Toast.LENGTH_SHORT).show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(view.getContext(), "Error postponing task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteTask(View view, Task task) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> actionListener.onDeleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }
}