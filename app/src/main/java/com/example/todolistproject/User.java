package com.example.todolistproject;

public class User {
    public String fullName, email;

    public User(String name, String email) {
        this.fullName = name;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
