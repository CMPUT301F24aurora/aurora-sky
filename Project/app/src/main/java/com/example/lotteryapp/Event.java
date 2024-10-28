package com.example.lotteryapp;

import android.widget.EditText;

public class Event {
    private String name;
    private String dateTime;
    private String numofPeople;
    private String description;

    public Event() {
    }

    public Event(String name, String dateTime, String numofPeople, String description) {
        this.name = name;
        this.dateTime = dateTime;
        this.numofPeople = numofPeople;
        this.description = description;
    }

//    public Event(EditText eventName, EditText eventDateTime, EditText eventNumberOfPeople, EditText eventDescription) {
//
//    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getNumofPeople() {
        return numofPeople;
    }

    public void setNumofPeople(String numofPeople) {
        this.numofPeople = numofPeople;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
