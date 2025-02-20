package com.example.todolistproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private final Sql dbHelper;

    public TaskDao(Context context) {
        dbHelper = new Sql(context);
    }

    public boolean addTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user_uid", task.getUserUid());
        values.put("date", task.getDate());
        values.put("description", task.getDescription());
        values.put("urgency", task.getUrgency());
        values.put("status", task.getStatus());
        values.put("post_to", task.getPostTo());

        long result = db.insert("tasks", null, values);
        db.close();

        return result != -1;
    }

    public List<Task> getTasksForUser(String userUid) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("tasks", null, "user_uid = ?", new String[]{userUid}, null, null, "date ASC");

        if (cursor.moveToFirst()) {
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
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public boolean updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("date", task.getDate());
        values.put("description", task.getDescription());
        values.put("urgency", task.getUrgency());
        values.put("status", task.getStatus());
        values.put("post_to", task.getPostTo());

        int rowsAffected = db.update("tasks", values, "id = ?", new String[]{String.valueOf(task.getId())});
        db.close();

        return rowsAffected > 0;
    }

    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete("tasks", "id = ?", new String[]{String.valueOf(taskId)});
        db.close();

        return rowsDeleted > 0;
    }

    public List<Task> getTasksByStatus(String userUid, int status) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("tasks", null,
                "user_uid = ? AND status = ?", new String[]{userUid, String.valueOf(status)},
                null, null, "date ASC");

        if (cursor.moveToFirst()) {
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
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public List<Task> getTasksByDate(String userUid, String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("tasks", null,
                "user_uid = ? AND date = ?", new String[]{userUid, date},
                null, null, "urgency DESC");

        if (cursor.moveToFirst()) {
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
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query("tasks", null, null, null, null, null, "date ASC");

        if (cursor.moveToFirst()) {
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
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }
}