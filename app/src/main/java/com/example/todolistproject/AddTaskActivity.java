package com.example.todolistproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextDescription, editTextDate, editTextPostponedTo;
    private Spinner spinnerUrgency, spinnerStatus;
    private Button btnSaveTask;
    private Sql dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        dbHelper = new Sql(this);

        // קישור בין הרכיבים ב-XML לקוד
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        editTextDate = findViewById(R.id.editTextTaskDate);
        editTextPostponedTo = findViewById(R.id.editTextPostponedTo);
        spinnerUrgency = findViewById(R.id.spinnerUrgency);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // הגדרת אפשרויות ל-Spinners
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Normal", "Urgent", "Very Urgent"});
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgency.setAdapter(urgencyAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Pending", "Completed", "Not Completed"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // לחיצה על כפתור השמירה
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void saveTask() {
        String description = editTextDescription.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String postponedTo = editTextPostponedTo.getText().toString().trim();
        int urgency = spinnerUrgency.getSelectedItemPosition();  // 0 - Normal, 1 - Urgent, 2 - Very Urgent
        int status = spinnerStatus.getSelectedItemPosition();    // 0 - Pending, 1 - Completed, 2 - Not Completed

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill in the description and date", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userUid = currentUser.getUid();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("date", date);
        values.put("description", description);
        values.put("urgency", urgency);
        values.put("status", status);
        values.put("post_to", postponedTo.isEmpty() ? null : postponedTo);
        values.put("user_uid", userUid);

        long result = db.insert("tasks", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Task saved successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddTaskActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
        }
    }
}