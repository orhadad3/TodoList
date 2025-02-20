package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> taskList;
    private final OnTaskActionListener actionListener;

    // Interface for handling task actions (edit, delete)
    public interface OnTaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener actionListener, Context context) {
        this.taskList = taskList;
        this.actionListener = actionListener;
        new TaskDao(context);
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

        // Check if task date has passed and update status if necessary
        updateTaskStatusIfOverdue(holder.itemView.getContext(), task);

        // Set task description
        holder.textViewDescription.setText(task.getDescription());

        // Set task date with "(Postponed)" in blue if postponed
        String dateText = "Date: " + task.getDate();
        if (task.getStatus() == 3) { // Postponed
            dateText += " (Postponed)";
            holder.textViewDate.setTextColor(Color.BLUE);
        } else {
            holder.textViewDate.setTextColor(Color.BLACK);
        }
        holder.textViewDate.setText(dateText);

        // Set urgency
        holder.textViewUrgency.setText("Urgency: " + getUrgencyString(task.getUrgency()));
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

        // Set status
        holder.textViewStatus.setText("Status: " + getStatusString(task.getStatus()));
        switch (task.getStatus()) {
            case 1: // Completed
                holder.textViewStatus.setTextColor(Color.GREEN);
                break;
            case 2: // Not Completed
                holder.textViewStatus.setTextColor(Color.RED);
                break;
            case 3: // Postponed
                holder.textViewStatus.setTextColor(Color.GRAY);
                break;
            default:
                holder.textViewStatus.setTextColor(Color.GRAY);
        }

        // Handle long press for options
        holder.itemView.setOnLongClickListener(v -> {
            showTaskOptionsDialog(holder.itemView, task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateTaskStatusIfOverdue(Context context, Task task) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date taskDate = sdf.parse(task.getDate());

            // Get today's date and subtract one day for comparison
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -1); // Yesterday
            Date yesterday = calendar.getTime();

            // Check if task date is before today
            if (taskDate != null && taskDate.before(yesterday) && task.getStatus() != 1 && task.getStatus() != 2) {
                // If the due date has passed (before today) and task is neither Completed nor Not Completed
                task.setStatus(2); // Set status to Not Completed

                TaskDao taskDao = new TaskDao(context);
                taskDao.updateTask(task);

                Toast.makeText(context, "Task \"" + task.getDescription() + "\" marked as Not Completed", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String statusOption = (task.getStatus() == 1) ? "Mark as Pending" : "Mark as Completed";

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Task Options")
                .setItems(new CharSequence[]{"Edit", "Postpone", statusOption, "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        editTask(view, task); // Go to Edit
                    } else if (which == 1) {
                        showPostponeDialog(view, task);
                    } else if (which == 2) {
                        if (task.getStatus() == 1) {
                            markTaskAsPending(view, task);
                        } else {
                            markTaskAsCompleted(view, task);
                        }
                    } else if (which == 3) {
                        confirmDeleteTask(view, task);
                    }
                }).show();
    }

    private void editTask(View view, Task task) {
        Intent intent = new Intent(view.getContext(), AddTaskActivity.class);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_description", task.getDescription());
        intent.putExtra("task_date", task.getDate());
        intent.putExtra("task_urgency", task.getUrgency());
        intent.putExtra("task_status", task.getStatus());
        view.getContext().startActivity(intent);
    }

    private void markTaskAsPending(View view, Task task) {
        task.setStatus(0); // 0 = Pending

        TaskDao taskDao = new TaskDao(view.getContext());
        boolean updated = taskDao.updateTask(task);

        if (updated) {
            notifyDataSetChanged();
            Toast.makeText(view.getContext(), "Task marked as Pending", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void markTaskAsCompleted(View view, Task task) {
        task.setStatus(1); // 1 = Completed

        TaskDao taskDao = new TaskDao(view.getContext());
        boolean updated = taskDao.updateTask(task);

        if (updated) {
            notifyDataSetChanged();
            Toast.makeText(view.getContext(), "Task marked as Completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPostponeDialog(View view, Task task) {
        try {
            final Calendar calendar = Calendar.getInstance();

            // Parse the current task date
            String currentTaskDate = task.getDate();
            String[] dateParts = currentTaskDate.split("/");

            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based in Calendar
                int year = Integer.parseInt(dateParts[2]);

                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
            }

            // Open DatePickerDialog with the task's date selected
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