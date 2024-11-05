package com.example.lotteryapp;

public class Organizer extends User {

    // Default constructor
    public Organizer() {}

    // Constructor with parameters
    public Organizer(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Organizer ID: " + getId());
        System.out.println("Organizer Name: " + getName());
        System.out.println("Organizer Email: " + getEmail());
        System.out.println("Organizer Phone: " + getPhone());
    }


    public boolean hasOrganizerPermissions() {
        return true;
    }
}
