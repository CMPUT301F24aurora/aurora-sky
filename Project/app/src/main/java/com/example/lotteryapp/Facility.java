package com.example.lotteryapp;

import java.io.Serializable;

/**
 * The {@code Facility} class represents a facility managed by an organizer.
 * It includes details such as the organizer ID, facility name, operating hours, location, and contact email.
 *
 * @version v1
 * @author Team Aurora
 */
public class Facility implements Serializable {

    private String organizerId;
    private String name;
    private String startTime;
    private String endTime;
    private String location;
    private String email;

    public Facility() {}

    public Facility(String organizerId, String name, String startTime, String endTime, String location, String email) {
        this.organizerId = organizerId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.email = email;
    }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}