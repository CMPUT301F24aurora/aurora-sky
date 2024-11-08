package com.example.lotteryapp;

public class Organizer extends User {

    public Organizer(String name, String email, String password) {
        super(name, email, password, "organizer"); // Assuming "organizer" is the role
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Organizer Info: " + getName());
    }

    public boolean hasOrganizerPermissions() {
        return "organizer".equals(getRole());
    }
}
