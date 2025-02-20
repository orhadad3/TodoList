package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private FloatingActionButton btnAddTask;
    private TaskDao taskDao;
    private TextView textViewUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        btnAddTask = findViewById(R.id.btnAddTask);
        textViewUserName = findViewById(R.id.textViewUserName);
        taskDao = new TaskDao(this);

        taskList = new ArrayList<>();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        loadUserName();

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

        FirebaseAnalytics.getInstance(this).logEvent("screen_view", null);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

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

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksFromDatabase() {
        taskList.clear();
        taskList.addAll(taskDao.getAllTasks());
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