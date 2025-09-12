package com.example.myapp;

public class Plan {
    private int id;
    private String title;
    private String description;
    private String date;
    private int userId;

    public Plan() {}

    public Plan(String title, String description, String date, int userId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.userId = userId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}