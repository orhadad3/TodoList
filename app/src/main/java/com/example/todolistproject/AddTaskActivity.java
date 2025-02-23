package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

/**
 * AddTaskActivity allows users to add or edit tasks in the to-do list.
 * It provides fields for description, date, urgency, and status.
 */
public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextDescription;
    private TextView textViewDate;
    private Spinner spinnerUrgency, spinnerStatus;
    private Sql dbHelper;

    /**
     * Initializes the activity and sets up UI components.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize database helper
        dbHelper = new Sql(this);

        // Initialize UI components
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        textViewDate = findViewById(R.id.textViewDate);
        spinnerUrgency = findViewById(R.id.spinnerUrgency);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        Button btnSaveTask = findViewById(R.id.btnSaveTask);

        // Set up urgency spinner
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Normal", "Urgent", "Very Urgent"});
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgency.setAdapter(urgencyAdapter);

        // Set up status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Pending", "Completed", "Not Completed"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Set date picker for date selection
        textViewDate.setOnClickListener(v -> showDatePicker(textViewDate));

        // Check if editing an existing task
        Intent intent = getIntent();
        if (intent.hasExtra("task_id")) {
            loadTaskForEditing(intent);
        }

        // Save task button action
        btnSaveTask.setOnClickListener(v -> saveTask());

        // Adjust layout for system windows
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Loads an existing task into the form for editing.
     *
     * @param intent The intent containing task data.
     */
    private void loadTaskForEditing(Intent intent) {
        if (intent.hasExtra("task_id")) {
            editTextDescription.setText(intent.getStringExtra("task_description"));

            // Prioritize 'post_to' over 'date'
            String taskDate = intent.getStringExtra("post_to");
            if (taskDate == null || taskDate.isEmpty()) {
                taskDate = intent.getStringExtra("task_date");
            }

            textViewDate.setText(taskDate);

            int urgency = intent.getIntExtra("task_urgency", 0);
            int status = intent.getIntExtra("task_status", 0);

            spinnerUrgency.setSelection(urgency);
            spinnerStatus.setSelection(status);
        }
    }

    /**
     * Displays a date picker dialog for selecting a task date.
     *
     * @param textView The TextView to display the selected date.
     */
    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();

        String existingDate = textView.getText().toString();
        if (!existingDate.isEmpty() && !existingDate.equals("Select Date")) {
            String[] dateParts = existingDate.split("/");
            if (dateParts.length == 3) {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]) - 1;
                int year = Integer.parseInt(dateParts[2]);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    @SuppressLint("DefaultLocale")
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    textView.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Restrict to future dates only
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    /**
     * Saves a new task or updates an existing one in the database.
     */
    private void saveTask() {
        try {
            String description = editTextDescription.getText().toString().trim();
            String selectedDate = textViewDate.getText().toString().trim();
            int urgency = spinnerUrgency.getSelectedItemPosition();
            int status = spinnerStatus.getSelectedItemPosition();

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.equals("Select Date")) {
                Toast.makeText(this, "Please select a date for the task", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userUid = currentUser.getUid();
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Check if it's an edit operation
            String taskId = getIntent().hasExtra("task_id") ? String.valueOf(getIntent().getIntExtra("task_id", -1)) : null;

            // Get original date if editing
            String originalDate = getIntent().getStringExtra("task_date");

            // If it's a new task, set the original date to the selected date
            if (originalDate == null || originalDate.isEmpty()) {
                originalDate = selectedDate;
            }

            // Duplicate check
            String duplicateCheckQuery = "SELECT * FROM tasks WHERE user_uid = ? AND description = ? AND id != ?";
            Cursor cursor = db.rawQuery(duplicateCheckQuery, new String[]{userUid, description, taskId != null ? taskId : "-1"});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String existingDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String existingPostTo = cursor.getString(cursor.getColumnIndexOrThrow("post_to"));

                    String effectiveDate = (existingPostTo != null && !existingPostTo.isEmpty()) ? existingPostTo : existingDate;

                    if (effectiveDate.equals(selectedDate)) {
                        cursor.close();
                        Toast.makeText(this, "A task with the same description and date already exists.", Toast.LENGTH_SHORT).show();
                        db.close();
                        return;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            ContentValues values = new ContentValues();
            values.put("description", description);
            values.put("date", originalDate);
            values.put("urgency", urgency);
            values.put("status", status);
            values.put("user_uid", userUid);

            // Handle post_to (only set if the selected date differs from the original)
            if (!selectedDate.equals(originalDate)) {
                values.put("post_to", selectedDate);
            } else {
                values.putNull("post_to");
            }

            // Insert or Update Task
            if (taskId != null && !taskId.equals("-1")) {
                int rowsAffected = db.update("tasks", values, "id = ?", new String[]{taskId});
                if (rowsAffected > 0) {
                    Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            } else {
                long result = db.insert("tasks", null, values);
                if (result != -1) {
                    Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
                }
            }

            db.close();
            startActivity(new Intent(AddTaskActivity.this, MainActivity.class));
            finish();
        } catch (Exception e) {
            Log.e("AddTaskActivity", "Error saving task: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}