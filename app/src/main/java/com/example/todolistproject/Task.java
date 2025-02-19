package com.example.todolistproject;

public class Task {
    private int id;
    private String date;
    private String description;
    private int urgency;
    private int status;
    private String postTo;
    private String userUid;

    public Task(int id, String date, String description, int urgency, int status, String postTo, String userUid) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.urgency = urgency;
        this.status = status;
        this.postTo = postTo;
        this.userUid = userUid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPostTo() {
        return postTo;
    }

    public void setPostTo(String postTo) {
        this.postTo = postTo;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }
}
