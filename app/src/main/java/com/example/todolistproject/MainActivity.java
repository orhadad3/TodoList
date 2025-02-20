package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private TaskDao taskDao;
    private TextView textViewUserName;
    private TextView textViewNoTasks;
    private RecyclerView recyclerViewTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        textViewNoTasks = findViewById(R.id.textViewNoTasks);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        textViewUserName = findViewById(R.id.textViewUserName);
        Button btnLogout = findViewById(R.id.btnLogout);

        taskDao = new TaskDao(this);
        taskList = new ArrayList<>();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        loadUserName();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                editTask(task);
            }

            @Override
            public void onDeleteTask(Task task) {
                taskDao.deleteTask(task.getId());
                loadTasksFromDatabase();
                Toast.makeText(MainActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
            }
        }, this);

        recyclerViewTasks.setAdapter(taskAdapter);

        loadTasksFromDatabase();

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        FloatingActionButton btnFilterTasks = findViewById(R.id.btnFilterTasks);

        if (btnFilterTasks != null) {
            btnFilterTasks.setOnClickListener(this::showFilterMenu);
        } else {
            Log.e("MainActivity", "btnFilterTasks is null");
        }

        FirebaseAnalytics.getInstance(this).logEvent("screen_view", null);
    }

    @SuppressLint("SetTextI18n")
    private void loadUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = currentUser.getEmail();
            }
            textViewUserName.setText("Welcome, " + displayName);
        }
    }

    private void showFilterMenu(View view) {
        // יצירת PopupMenu שמזהה את ה-XML בתיקיית menu
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());

        // מאזין לאירועי לחיצה על הפריטים בתפריט
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.filter_by_day) {
                showDatePickerDialog();
                return true;
            } else if (item.getItemId() == R.id.filter_completed) {
                filterTasksByStatus(1); // Completed
                return true;
            } else if (item.getItemId() == R.id.filter_not_completed) {
                filterTasksByStatus(2); // Not Completed
                return true;
            } else if (item.getItemId() == R.id.filter_all) {
                loadTasksFromDatabase();
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }

    private void filterTasksByStatus(int status) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        List<Task> filteredTasks = taskDao.getTasksByStatus(userUid, status);
        taskList.clear();
        taskList.addAll(filteredTasks);

        if (taskList.isEmpty()) {
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }

        taskAdapter.notifyDataSetChanged();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    @SuppressLint("DefaultLocale")
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    filterTasksByDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void filterTasksByDate(String date) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        List<Task> filteredTasks = taskDao.getTasksByDate(userUid, date);
        taskList.clear();
        taskList.addAll(filteredTasks);

        if (taskList.isEmpty()) {
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }

        taskAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksFromDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        taskList.clear();
        taskList.addAll(taskDao.getTasksForUser(userUid));

        if (taskList.isEmpty()) {
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }

        taskAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase();
    }

    private void editTask(Task task) {
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra("task_id", task.getId());
        startActivity(intent);
    }
}