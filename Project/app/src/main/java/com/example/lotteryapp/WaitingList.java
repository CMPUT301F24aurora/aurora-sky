package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaitingList implements Serializable {
    private List<String> waitingListIds;  // Stores entrant IDs
    private Optional<Integer> capacity;   // Optional maximum number of entrants allowed
    private String eventId;  // The ID of the event this waiting list belongs to
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Constructor for unlimited capacity
    public WaitingList(String eventId) {
        this.waitingListIds = new ArrayList<>();
        this.capacity = Optional.empty();
        this.eventId = eventId;
        loadFromDatabase(); // Synchronously load the waiting list
    }

    // Constructor with optional capacity limit
    public WaitingList(String eventId, int capacity) {
        this.waitingListIds = new ArrayList<>();
        this.capacity = capacity > 0 ? Optional.of(capacity) : Optional.empty();
        this.eventId = eventId;
        loadFromDatabase(); // Synchronously load the waiting list
    }

    // Load the waiting list IDs from the database
    private void loadFromDatabase() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> ids = (List<String>) document.get("waitingList");
                        waitingListIds = ids != null ? new ArrayList<>(ids) : new ArrayList<>();
                    }
                })
                .addOnFailureListener(e -> waitingListIds = new ArrayList<>()); // Initialize empty list on failure
    }

    // Add an entrant ID to the waiting list
    public boolean addEntrant(String entrantId) {
        if (capacity.isPresent() && waitingListIds.size() >= capacity.get()) {
            return false; // List is full
        }

        if (!waitingListIds.contains(entrantId)) {
            waitingListIds.add(entrantId);
            updateDatabase();
            return true;
        }
        return false; // Entrant already in the list
    }

    // Remove an entrant ID from the waiting list
    public boolean removeEntrant(String entrantId) {
        if (waitingListIds.contains(entrantId)) {
            waitingListIds.remove(entrantId);
            updateDatabase();
            return true;
        }
        return false; // Entrant not in the list
    }

    // Get the current size of the waiting list
    public int size() {
        return waitingListIds.size();
    }

    // Check if the waiting list is full
    public boolean isFull() {
        return capacity.isPresent() && waitingListIds.size() >= capacity.get();
    }

    // Helper method to update the waiting list in the database
    private void updateDatabase() {
        db.collection("events").document(eventId)
                .update("waitingList", waitingListIds)
                .addOnFailureListener(e -> System.out.println("Failed to update database: " + e.getMessage()));
    }

    // Get the waiting list capacity
    public Optional<Integer> getCapacity() {
        return capacity;
    }

    // Get the list of waiting list IDs
    public List<String> getWaitingListIds() {
        return new ArrayList<>(waitingListIds);
    }
}
