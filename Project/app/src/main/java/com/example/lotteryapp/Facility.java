package com.example.lotteryapp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;

public class Facility implements Serializable {

    private String id;
    private String name;
    private String time;
    private String location;
    private String email;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Facility() {}

    public Facility(String name, String time, String location, String email) {
        this.name = name;
        this.time = time;
        this.location = location;
        this.email = email;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void saveToFirestore(FacilityCallback callback) {
        if (id == null || id.isEmpty()) {
            // Create a new document with an auto-generated ID
            db.collection("facilities")
                    .add(this)
                    .addOnSuccessListener(documentReference -> {
                        String documentId = documentReference.getId();
                        this.setId(documentId);// Save generated ID back to the object
                        callback.onSuccess(documentId);
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            // Update an existing document with the provided ID
            db.collection("facilities").document(this.id)
                    .set(this)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(this.id))
                    .addOnFailureListener(callback::onFailure);
        }
    }

    public void updateInFirestore(FacilityCallback callback) {
        db.collection("facilities").document(this.id)
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess(this.id))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteFromFirestore(FacilityCallback callback) {
        db.collection("facilities").document(this.id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(this.id))
                .addOnFailureListener(callback::onFailure);
    }

    public interface FacilityCallback {
        void onSuccess(String documentId);
        void onFailure(Exception e);
    }
}
