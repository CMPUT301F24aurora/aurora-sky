package com.example.lotteryapp;

import java.io.Serializable;
import com.google.firebase.firestore.FirebaseFirestore;

public class Facility implements Serializable {

    private String id;
    private String name;
    private String time;
    private String location;
    private String email;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();


    public Facility() {}


    public Facility(String id, String name, String time, String location, String email) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.location = location;
        this.email = email;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void saveToFirestore(FacilityCallback callback) {
        // Check if the id is null to determine if this is a new facility
        if (id == null || id.isEmpty()) {
            db.collection("facilities").add(this)
                    .addOnSuccessListener(documentReference -> {
                        this.id = documentReference.getId(); // Set ID for future reference
                        callback.onSuccess("Facility created successfully with ID: " + id);
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            updateInFirestore(callback);
        }
    }


    public void updateInFirestore(FacilityCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onFailure(new Exception("Facility ID is null or empty; cannot update"));
            return;
        }
        db.collection("facilities").document(this.id)
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Facility updated successfully"))
                .addOnFailureListener(callback::onFailure);
    }


    public void deleteFromFirestore(FacilityCallback callback) {
        if (id == null || id.isEmpty()) {
            callback.onFailure(new Exception("Facility ID is null or empty; cannot delete"));
            return;
        }
        db.collection("facilities").document(this.id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Facility removed successfully"))
                .addOnFailureListener(callback::onFailure);
    }


    public interface FacilityCallback {
        void onSuccess(String message);
        void onFailure(Exception e);
    }
}
