package com.example.lotteryapp;

import java.util.ArrayList;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;

public class Event {
    //Donna did the XML file

    String posterImage;
    String eventName;
    Date eventDate;
    ArrayList<Entrant> attendees;
    String eventQRHash;
    ArrayList<Entrant> waitingList;
    Boolean geolocationRequired;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Event(){
        //Empty constructor for Firebase
    }

    public void updatePoster(){

    }

    public Event(String eventName, Date eventDate, String posterImage, Boolean geolocationRequired) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.posterImage = posterImage;
        this.eventQRHash = generateQRHash();
        this.geolocationRequired = geolocationRequired;
    }

    public String getName() {
        return eventName;
    }

    public Date getEventDate() {
        return eventDate;
    }

    private String generateQRHash() {
        // To-do: Implement QR code generation and hashing logic
        return "temp";
    }

    public boolean getGeolocationRequirement(){
        return geolocationRequired;
    }

    public String getEventQRHash(){
        return eventQRHash;
    }

    public void setName(String eventName){
        this.eventName = eventName;
    }

    public void setEventDate(Date eventDate){
        this.eventDate = eventDate;
    }

    public void setGeolocationRequired(Boolean geolocationRequired){
        this.geolocationRequired = geolocationRequired;
    }

    // Method to check if the event exists in Firestore
    public static void checkEntrantExists(String deviceId, EntrantCheckCallback callback) {
        db.collection("events").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        callback.onEntrantExists(entrant);
                    } else {
                        callback.onEntrantNotFound();
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    // Save Event object to Firestore
    public void saveToFirestore(SaveEntrantCallback callback) {
        db.collection("events").document(this.getEventQRHash())
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Event Identifier: " + getEventQRHash());
        System.out.println("Event title: " + getName());
    }
}
