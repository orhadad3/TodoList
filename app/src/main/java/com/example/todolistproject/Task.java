package com.example.todolistproject;

/**
 * The Task class represents a task in the to-do list application.
 * It contains all the necessary fields to describe a task, including its ID,
 * date, description, urgency, status, post destination, and associated user UID.
 */
public class Task {
    // Unique identifier for the task
    private int id;
    // Date of the task
    private final String date;
    // Description of the task
    private final String description;
    // Urgency level of the task (e.g., 0 - Normal, 1 - Urgent, 2 - Very Urgent)
    private final int urgency;
    // Status of the task (e.g., 0 - Pending, 1 - Completed, 2 - Not Completed, 3 - Postponed)
    private int status;
    // Optional field indicating where to post the task
    private String postTo;
    // User UID associated with the task
    private final String userUid;


    /**
     * Constructor to initialize a Task object.
     *
     * @param id          Unique identifier for the task.
     * @param date        Date of the task.
     * @param description Description of the task.
     * @param urgency     Urgency level of the task.
     * @param status      Status of the task.
     * @param postTo      Destination to post the task.
     * @param userUid     User UID associated with the task.
     */
    public Task(int id, String date, String description, int urgency, int status, String postTo, String userUid) {
        this.id = id;
        this.date = date;
        this.description = description;
        this.urgency = urgency;
        this.status = status;
        this.postTo = postTo;
        this.userUid = userUid;
    }

    // Getter and setter methods for each field

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getUrgency() {
        return urgency;
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
}
