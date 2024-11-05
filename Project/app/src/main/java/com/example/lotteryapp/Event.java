package com.example.lotteryapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Event implements Serializable {
    //Donna did the XML file

    private String posterImage;
    private String eventName;

    //for now event date is a string
    private String eventDate;
    private ArrayList<Entrant> attendees;
//    private String eventQRHash;
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

    public String generateQRHash(String eventId) {
        // To-do: Implement QR code generation and hashing logic
        // Use eventQRHash as unique identifier
        String firebaseUrl = "https://yourfirebaseproject.firebaseio.com/events/" + eventId;

        // Generate QR code bitmap
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode(firebaseUrl, BarcodeFormat.QR_CODE, 200, 200);
            //Display the QR code
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        // Convert BitMatrix to string (for simplicity)
        StringBuilder qrData = new StringBuilder();
        for (int y = 0; y < bitMatrix.getHeight(); y++) {
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                qrData.append(bitMatrix.get(x, y) ? "1" : "0");
            }
        }

        // Generate SHA-256 hash
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(qrData.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
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
                    callback.onSuccess(documentReference.getId());
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
