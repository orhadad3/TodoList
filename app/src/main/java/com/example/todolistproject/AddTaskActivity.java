package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextDescription;
    private TextView textViewDate;
    private Spinner spinnerUrgency, spinnerStatus;
    private Sql dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        dbHelper = new Sql(this);

        // Initialize views
        editTextDescription = findViewById(R.id.editTextTaskDescription);
        textViewDate = findViewById(R.id.textViewDate);
        spinnerUrgency = findViewById(R.id.spinnerUrgency);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        Button btnSaveTask = findViewById(R.id.btnSaveTask);

        // Set up spinners
        ArrayAdapter<String> urgencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Normal", "Urgent", "Very Urgent"});
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgency.setAdapter(urgencyAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Pending", "Completed", "Not Completed"});
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        textViewDate.setOnClickListener(v -> showDatePicker(textViewDate));

        // Save task button
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            @SuppressLint("DefaultLocale")
            String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
            textView.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }


    private void saveTask() {
        String description = editTextDescription.getText().toString().trim();
        String date = textViewDate.getText().toString().trim();
        int urgency = spinnerUrgency.getSelectedItemPosition();
        int status = spinnerStatus.getSelectedItemPosition();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(date) || date.equals("Select Date")) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
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
