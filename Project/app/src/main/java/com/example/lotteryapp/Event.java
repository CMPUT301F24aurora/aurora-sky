package com.example.lotteryapp;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class Event implements Serializable {

    private String posterImage;
    private String eventName;
    private String eventDate;
    private Boolean geolocationRequired = Boolean.FALSE;
    private Integer numPeople;
    private String description;
    private ArrayList<Entrant> waitingList;
    private String qr_code;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Event() {
        // Empty constructor for Firebase
    }

    public Event(String eventName, String eventDate, Integer numPeople, String description) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.numPeople = numPeople;
        this.description = description;
        this.waitingList = new ArrayList<>();
        this.qr_code = generateQRHash();
    }

    public String getName() {
        return eventName;
    }

    public void setName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return this.eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public int getNumPeople() {
        return this.numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getQR_code(){
        return this.qr_code;
    }

    public boolean getGeolocationRequirement() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }


    public ArrayList<Entrant> getWaitingList() {
        return waitingList;
    }

    // Method to add an entrant to the waiting list if the event is full
    public boolean addEntrantToWaitingList(Entrant entrant, WaitingListCallback callback) {
        if (waitingList.contains(entrant)) {
            callback.onFailure(new Exception("Entrant is already in the waiting list."));
            return false; // Entrant is already in the waiting list
        }

        waitingList.add(entrant); // Add the entrant to the waiting list
        String eventHash = this.getQR_code();
        // Update Firestore to save the waiting list
        db.collection("events").document(eventHash)
                .update("waitingList", waitingList)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Entrant added to the waiting list."))
                .addOnFailureListener(callback::onFailure);

        return true;
    }

    public void removeEntrantFromWaitingList(Entrant entrant, WaitingListCallback callback) {
        if (!waitingList.contains(entrant)) {
            callback.onFailure(new Exception("Entrant is not in the waiting list."));
            return; // Entrant is not in the waiting list
        }

        waitingList.remove(entrant); // Remove the entrant from the waiting list
        String eventHash = this.getQR_code();

        // Update Firestore to save the updated waiting list
        db.collection("events").document(eventHash)
                .update("waitingList", waitingList)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Entrant removed from the waiting list."))
                .addOnFailureListener(callback::onFailure);
    }

    // Method to remove an entrant from the waiting list
    public boolean removeEntrantFromWaitingList(Entrant entrant) {
        return waitingList.remove(entrant);
    }

    // Method to generate a QR code hash for the event
    public String generateQRHash() {
        String uniqueIdentifier = eventName + eventDate + System.currentTimeMillis();
        String firebaseUrl = "https://yourfirebaseproject.firebaseio.com/events/" + uniqueIdentifier;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode(firebaseUrl, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder qrData = new StringBuilder();
        for (int y = 0; y < bitMatrix.getHeight(); y++) {
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                qrData.append(bitMatrix.get(x, y) ? "1" : "0");
            }
        }

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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Save Event object to Firebase
    public void saveToFirestore(SaveEventCallback callback) {
        String eventHash = this.getQR_code(); // Unique hash including timestamp

        db.collection("events").document(eventHash).set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess(eventHash))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    // Get events from Firebase
    public static void getEventsFromFirestore(GetEventsCallback callback) {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> events = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            events.add(event);
                        }
                        callback.onSuccess(events);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

}
