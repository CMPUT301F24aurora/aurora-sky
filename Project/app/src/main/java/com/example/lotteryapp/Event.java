package com.example.lotteryapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;

public class Event implements Serializable {
    //Donna did the XML file

    private String posterImage;
    private String eventName;

    //for now event date is a string
    private String eventDate;
    private ArrayList<Entrant> attendees;
//    String eventQRHash;
    private ArrayList<Entrant> waitingList;
    private Boolean geolocationRequired = Boolean.FALSE;
    private Integer numPeople;
    private String description;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Event(){
        //Empty constructor for Firebase
    }

    public Event(String eventName, String eventDate, Integer numPeople, String description) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.numPeople = numPeople;
        this.description = description;
    }

    public String getName() {
        return eventName;
    }

    public void setName(String eventName){
        this.eventName = eventName;
    }

    private String generateQRHash() {
        // To-do: Implement QR code generation and hashing logic
        return "temp";
    }

    public String getEventDate(){
        return this.eventDate;
    }

    public void setEventDate(String eventDate){
        this.eventDate = eventDate;
    }

    public int getNumPeople(){
        return this.numPeople;
    }

    public void setNumPeople(int numPeople){
        this.numPeople = numPeople;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean getGeolocationRequirement(){
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired){
        this.geolocationRequired = geolocationRequired;
    }

    // Save Event object to Firestore
    public void saveToFirestore(SaveEventCallback callback) {
        // Use Firestore's automatic ID generation
        db.collection("events").add(this)
                .addOnSuccessListener(documentReference -> {
                    // Optionally, you can pass the document ID back through the callback if needed
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    public void deleteEvent(String eventQRHash){

    }

//    public void displayEventInfo() {
//        System.out.println("Event Identifier: " + getEventQRHash());
//        System.out.println("Event title: " + getName());
//    }
}
