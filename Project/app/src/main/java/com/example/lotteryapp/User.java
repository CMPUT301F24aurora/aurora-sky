package com.example.lotteryapp;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;  // Role to differentiate organizer and entrant

    // Default constructor required for Firestore
    public User() {
    }

    // Constructor with parameters for organizers (with role)
    public User(String id, String name, String email, String phone, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Constructor with parameters for entrants (without role or phone)
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Constructor with parameters for entrants (with phone)
    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Abstract method for displaying user info
    public abstract void displayUserInfo();
}
