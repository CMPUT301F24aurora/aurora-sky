package com.example.lotteryapp;

public class Entrant extends User {
    // Default constructor required for Firestore
    public Entrant() {
    }

    // Constructor with parameters
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    // Constructor with parameters
    public Entrant(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }
}
