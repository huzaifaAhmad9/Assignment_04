package com.example.assignment_04;

public class User {
    private String name;
    private int id; // Updated to int
    private String description;
    private String profileImageUrl;

    public User() {
        // Default constructor required for DataSnapshot.getValue(User.class)
    }

    public User(String name, String description, String imageUrl, int id) {
        this.name = name;
        this.description = description;
        this.profileImageUrl = imageUrl;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
