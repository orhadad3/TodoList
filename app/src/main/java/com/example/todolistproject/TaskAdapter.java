package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * TaskAdapter is a RecyclerView adapter that binds Task data to views in a list.
 * It handles displaying task details, updating task status, and task interactions.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private final OnTaskActionListener actionListener;

    /**
     * Interface for handling task actions (edit, delete).
     */
    public interface OnTaskActionListener {
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }

    /**
     * Constructor for TaskAdapter.
     *
     * @param taskList      List of tasks to display.
     * @param actionListener Listener for task actions.
     * @param context        The context of the application.
     */
    public TaskAdapter(List<Task> taskList, OnTaskActionListener actionListener, Context context) {
        this.taskList = taskList;
        this.actionListener = actionListener;
        new TaskDao(context);
    }

    /**
     * ViewHolder class for Task items.
     */
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

        // Update status if task date has passed
        updateTaskStatusIfOverdue(holder.itemView.getContext(), task);

        // Set task description
        holder.textViewDescription.setText(task.getDescription());

        // Set task date and color if postponed
        String dateText;

        if (task.getPostTo() != null && !task.getPostTo().isEmpty()) {
            dateText = "Postponed to: " + task.getPostTo();
            holder.textViewDate.setTextColor(Color.BLUE);
        } else {
            dateText = "Date: " + task.getDate();
            holder.textViewDate.setTextColor(Color.BLACK);
        }

        holder.textViewDate.setText(dateText);

        // Set urgency and color (0 - Normal, 1 - Urgent, 2 - Very Urgent)
        holder.textViewUrgency.setText("Urgency: " + getUrgencyString(task.getUrgency()));
        switch (task.getUrgency()) {
            case 1:
                holder.textViewUrgency.setTextColor(Color.parseColor("#FFBC0D")); // Orange
                break;
            case 2:
                holder.textViewUrgency.setTextColor(Color.parseColor("#C20E0F")); // Red
                break;
            default:
                holder.textViewUrgency.setTextColor(Color.GRAY);
        }

        // Set status and color
        holder.textViewStatus.setText("Status: " + getStatusString(task.getStatus()));
        switch (task.getStatus()) {
            case 1:
                holder.textViewStatus.setTextColor(Color.parseColor("#8FCE00")); // Green
                break;
            case 2:
                holder.textViewStatus.setTextColor(Color.parseColor("#C20E0F")); // Red
                break;
            default:
                holder.textViewStatus.setTextColor(Color.GRAY);
        }

        // Set long click listener for task options
        holder.itemView.setOnLongClickListener(v -> {
            showTaskOptionsDialog(holder.itemView, task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Updates task status if the due date has passed.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateTaskStatusIfOverdue(Context context, Task task) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Use post_to if available, otherwise use date
            String taskDateString = (task.getPostTo() != null && !task.getPostTo().isEmpty()) ? task.getPostTo() : task.getDate();

            // Validate if the cleaned date matches the expected format
            if (taskDateString == null || !taskDateString.matches("\\d{2}/\\d{2}/\\d{4}")) {
                Log.e("TaskAdapter", "Invalid date format: " + taskDateString);
                return; // Skip invalid dates
            }

            Date taskDate = sdf.parse(taskDateString);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -1); // Yesterday
            Date yesterday = calendar.getTime();

            // Mark task as "Not Completed" if overdue and not already marked
            if (taskDate != null && taskDate.before(yesterday) && task.getStatus() != 1 && task.getStatus() != 2) {
                task.setStatus(2); // Mark as "Not Completed"

                TaskDao taskDao = new TaskDao(context);
                taskDao.updateTask(task);

                Toast.makeText(context, "Task \"" + task.getDescription() + "\" marked as Not Completed due to overdue date", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("TaskAdapter", "Error updating task status: " + e.getMessage(), e);
        }
    }

    /**
     * Converts urgency code to a readable string.
     */
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

    /**
     * Converts status code to a readable string.
     */
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

    /**
     * Displays task options dialog (Edit, Postpone, Complete/Pending, Delete).
     */
    private void showTaskOptionsDialog(View view, Task task) {
        String statusOption = (task.getStatus() == 1) ? "Mark as Pending" : "Mark as Completed";

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Task Options")
                .setItems(new CharSequence[]{"Edit", "Postpone", statusOption, "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        actionListener.onEditTask(task);
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

    /**
     * Marks a task as Pending.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void markTaskAsPending(View view, Task task) {
        task.setStatus(0);
        TaskDao taskDao = new TaskDao(view.getContext());
        boolean updated = taskDao.updateTask(task);

        if (updated) {
            notifyDataSetChanged();
            Toast.makeText(view.getContext(), "Task marked as Pending", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Marks a task as Completed.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void markTaskAsCompleted(View view, Task task) {
        task.setStatus(1);
        TaskDao taskDao = new TaskDao(view.getContext());
        boolean updated = taskDao.updateTask(task);

        if (updated) {
            notifyDataSetChanged();
            Toast.makeText(view.getContext(), "Task marked as Completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a dialog to postpone the task to a new date.
     */
    private void showPostponeDialog(View view, Task task) {
        try {
            final Calendar calendar = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date originalDate = sdf.parse(task.getDate());

            if (originalDate != null) {
                calendar.setTime(originalDate);
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), (datePicker, year, month, dayOfMonth) -> {
                @SuppressLint("DefaultLocale")
                String newPostponeDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);

                // Prevent postponing to an earlier date than the original
                try {
                    if (Objects.requireNonNull(sdf.parse(newPostponeDate)).before(originalDate)) {
                        Toast.makeText(view.getContext(), "Cannot postpone to an earlier date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    Log.e("TaskAdapter", "Error parsing date: " + e.getMessage(), e);
                    Toast.makeText(view.getContext(), "Error processing date", Toast.LENGTH_SHORT).show();
                    return;  // Handle the error gracefully instead of crashing
                }

                // Set post_to in DB
                task.setPostTo(newPostponeDate);

                TaskDao taskDao = new TaskDao(view.getContext());
                taskDao.updateTask(task);

                notifyItemChanged(taskList.indexOf(task));
                Toast.makeText(view.getContext(), "Task postponed to " + newPostponeDate, Toast.LENGTH_SHORT).show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // Ensure the minimum date is the original task date
            assert originalDate != null;
            datePickerDialog.getDatePicker().setMinDate(originalDate.getTime());
            datePickerDialog.show();

        } catch (Exception e) {
            Log.e("TaskAdapter", "Error postponing task", e);
            Toast.makeText(view.getContext(), "Error postponing task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Updates the task list displayed in the RecyclerView.
     * This method clears the existing list and adds the new filtered or updated tasks.
     *
     * @param newTaskList The list of tasks to display.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateTaskList(List<Task> newTaskList) {
        taskList.clear();
        taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    /**
     * Shows a confirmation dialog before deleting a task.
     */
    private void confirmDeleteTask(View view, Task task) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> actionListener.onDeleteTask(task))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
