package com.example.todolistproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * TaskDao class handles database operations for the Task entity.
 * This includes CRUD operations (Create, Read, Update, Delete)
 * using SQLite database.
 */
public class TaskDao {
    private final Sql dbHelper;

    /**
     * Constructor to initialize TaskDao with database helper.
     *
     * @param context The application context.
     */
    public TaskDao(Context context) {
        dbHelper = new Sql(context);
    }

    /**
     * Updates an existing task in the database.
     *
     * @param task The task object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateTask(Task task) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            ContentValues values = new ContentValues();

            values.put("date", task.getDate());
            values.put("description", task.getDescription());
            values.put("urgency", task.getUrgency());
            values.put("status", task.getStatus());
            values.put("post_to", task.getPostTo());

            int rowsAffected = db.update("tasks", values, "id = ?", new String[]{String.valueOf(task.getId())});
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("TaskDao", "Error updating task: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Deletes a task from the database.
     *
     * @param taskId The ID of the task to be deleted.
     * @return true if the task was deleted successfully, false otherwise.
     */
    public boolean deleteTask(int taskId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            int rowsDeleted = db.delete("tasks", "id = ?", new String[]{String.valueOf(taskId)});
            db.close();

            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("TaskDao", "Error deleting task: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Task> getFilteredTasks(String userUid, Integer status, String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            // Base query: Select tasks by user
            String selection = "user_uid = ?";
            List<String> selectionArgsList = new ArrayList<>();
            selectionArgsList.add(userUid);

            // Apply status filter if provided
            if (status != null) {
                selection += " AND status = ?";
                selectionArgsList.add(String.valueOf(status));
            }

            // Apply date filter if provided (considering post_to and date)
            if (date != null) {
                selection += " AND (post_to = ? OR date = ?)";
                selectionArgsList.add(date);
                selectionArgsList.add(date);
            }

            cursor = db.query("tasks", null, selection, selectionArgsList.toArray(new String[0]), null, null, "date ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Task task = new Task(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("urgency")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("status")),
                            cursor.getString(cursor.getColumnIndexOrThrow("post_to")),
                            cursor.getString(cursor.getColumnIndexOrThrow("user_uid"))
                    );

                    // Advanced filtering based on status
                    if (status != null) {
                        if (status == 1 && task.getStatus() == 1) {
                            // Completed tasks
                            taskList.add(task);
                        } else if (status == 2) {
                            // Not Completed + Overdue tasks
                            if (task.getStatus() == 2 || isTaskOverdue(task)) {
                                taskList.add(task);
                            }
                        }
                    } else {
                        taskList.add(task);
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TaskDao", "Error fetching tasks: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return taskList;
    }

    private boolean isTaskOverdue(Task task) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String taskDate = (task.getPostTo() != null && !task.getPostTo().isEmpty()) ? task.getPostTo() : task.getDate();

            Date parsedDate = sdf.parse(taskDate);
            Date today = new Date();

            return parsedDate != null && parsedDate.before(today) && task.getStatus() != 1;
        } catch (Exception e) {
            Log.e("TaskDao", "Error parsing task date: " + e.getMessage(), e);
            return false;
        }
    }

}