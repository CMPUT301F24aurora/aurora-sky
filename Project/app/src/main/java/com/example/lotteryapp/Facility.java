package com.example.lotteryapp;

import java.io.Serializable;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;

public class Facility implements Serializable {

   
    private String id;
    private String name;
    private String address;
    private int capacity;  
    private String type; 
    private List<String> eventsHosted; 
    public Facility() {
    }

   
    public Facility(String id, String name, String address, int capacity, String type) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.capacity = capacity;
        this.type = type;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getEventsHosted() {
        return eventsHosted;
    }

    public void setEventsHosted(List<String> eventsHosted) {
        this.eventsHosted = eventsHosted;
    }

    
    public void saveToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(this.id)
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Facility saved successfully.");
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error saving facility: " + e.getMessage());
                });
    }

    
    public static void getFacilityFromFirestore(String facilityId, FirestoreCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("facilities").document(facilityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Facility facility = documentSnapshot.toObject(Facility.class);
                        callback.onCallback(facility);
                    } else {
                        System.out.println("No such facility found.");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error retrieving facility: " + e.getMessage());
                });
    }

    
    public interface FirestoreCallback {
        void onCallback(Facility facility);
    }

    
    @Override
    public String toString() {
        return "Facility [id=" + id + ", name=" + name + ", address=" + address + ", capacity=" + capacity + ", type=" + type + "]";
    }
}
