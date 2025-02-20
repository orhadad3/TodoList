package com.example.todolistproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        fullNameEditText = findViewById(R.id.editTextFullName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        Button registerButton = findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(v -> registerFunc());
    }

    private void registerFunc() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if(fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter full name and email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter password and confirm password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                assert user != null;

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .setValue(new User(fullName, email))
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Failed to save user data: " + Objects.requireNonNull(dbTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to set display name: " + Objects.requireNonNull(profileTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } else {
                Toast.makeText(this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}