package com.example.todolistproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * MainActivity serves as the main screen of the to-do list app.
 * It displays a list of tasks, allows filtering, searching, and user logout.
 */
public class MainActivity extends AppCompatActivity {
    private TaskAdapter taskAdapter;
    private ArrayList<Task> taskList;
    private TaskDao taskDao;
    private TextView textViewUserName;
    private TextView textViewNoTasks;
    private RecyclerView recyclerViewTasks;
    private ArrayList<Task> currentDisplayedTasks;

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
        EditText editTextSearch = findViewById(R.id.editTextSearch);

        taskDao = new TaskDao(this);
        taskList = new ArrayList<>();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        loadUserName();

        taskAdapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEditTask(Task task) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("task_id", task.getId());
                intent.putExtra("task_description", task.getDescription());
                intent.putExtra("task_date", task.getDate());
                intent.putExtra("post_to", task.getPostTo());
                intent.putExtra("task_urgency", task.getUrgency());
                intent.putExtra("task_status", task.getStatus());
                startActivity(intent);
            }

            @Override
            public void onDeleteTask(Task task) {
                boolean isDeleted = taskDao.deleteTask(task.getId());

                if(isDeleted) {
                    loadTasksFromDatabase();
                    Toast.makeText(MainActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            }
        }, this);

        recyclerViewTasks.setAdapter(taskAdapter);

        // Load tasks AFTER initializing taskAdapter
        loadTasksFromDatabase();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        FloatingActionButton btnFilterTasks = findViewById(R.id.btnFilterTasks);
        if (btnFilterTasks != null) {
            btnFilterTasks.setOnClickListener(this::showFilterMenu);
        }

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasksByDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FirebaseAnalytics.getInstance(this).logEvent("task_view", null);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasksFromDatabase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        taskList.clear();
        taskList.addAll(taskDao.getFilteredTasks(userUid, null, null));

        currentDisplayedTasks = new ArrayList<>(taskList);
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        if (currentDisplayedTasks.isEmpty()) {
            textViewNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            textViewNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
        }
        taskAdapter.updateTaskList(currentDisplayedTasks);
    }

    private void showFilterMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.filter_by_day) {
                showDatePickerDialog();
                return true;
            } else if (item.getItemId() == R.id.filter_completed) {
                filterTasksByStatus(1);
                return true;
            } else if (item.getItemId() == R.id.filter_not_completed) {
                filterTasksByStatus(2);
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

    private void filterTasksByDescription(String query) {
        if (query.trim().isEmpty()) {
            taskAdapter.updateTaskList(currentDisplayedTasks);
            return;
        }

        ArrayList<Task> filteredList = new ArrayList<>();
        for (Task task : currentDisplayedTasks) {
            if (task.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(task);
            }
        }

        taskAdapter.updateTaskList(filteredList);
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

    private void filterTasksByStatus(int status) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        List<Task> filteredTasks = taskDao.getFilteredTasks(userUid, status, null);
        currentDisplayedTasks = new ArrayList<>(filteredTasks);
        taskAdapter.updateTaskList(currentDisplayedTasks);
    }

    private void filterTasksByDate(String date) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userUid = currentUser.getUid();
        List<Task> filteredTasks = taskDao.getFilteredTasks(userUid, null, date);
        currentDisplayedTasks = new ArrayList<>(filteredTasks);
        taskAdapter.updateTaskList(currentDisplayedTasks);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase();
    }
}
