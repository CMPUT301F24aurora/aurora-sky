package com.example.lotteryapp;

public class Entrant extends User {
    // Constructor
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }
}
