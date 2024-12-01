package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaitingList {
    private String eventId;  // The ID of the event this waiting list belongs to
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Integer waitlistCap;

    public WaitingList(String eventId){
        this.eventId = eventId;
        fetchWaitlistCap();
    }

    private void fetchWaitlistCap() {
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("waitlistCap")) {
                waitlistCap = documentSnapshot.getLong("waitlistCap").intValue();
            } else {
                waitlistCap = null; // No cap set
            }
        }).addOnFailureListener(e -> {
            waitlistCap = null; // Default to no cap on failure
        });
    }

    // Add or remove methods remain unchanged, but include callbacks for database operations
    public boolean addEntrant(String entrantId, List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        if (waitlistCap != null && waitingListIds.size() >= waitlistCap) {
            // Waitlist is full
            listener.onFailure(new Exception("Waitlist is full"));
            return false;
        }

        if (!waitingListIds.contains(entrantId)) {
            waitingListIds.add(entrantId);
            updateDatabase(waitingListIds, listener);
            return true;
        }

        return false;
//        if (waitingListIds.isEmpty()){
//            waitingListIds.add(entrantId);
//            updateDatabase(waitingListIds, listener);
//        } else if(!waitingListIds.isEmpty() && !waitingListIds.contains(entrantId)){
//            waitingListIds.add(entrantId);
//            updateDatabase(waitingListIds, listener);
//        }
//        return false;
    }

    public boolean removeEntrant(String entrantId, List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        if (waitingListIds.contains(entrantId)) {
            waitingListIds.remove(entrantId);
            updateDatabase( waitingListIds, listener); // Update Firestore with a callback
            return true;
        }
        return false;
    }

    // Updated database with callback to notify success/failure
    private void updateDatabase(List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        db.collection("events").document(eventId)
                .update("waitingList", waitingListIds)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    // Define callback interface for database update events
    public interface OnDatabaseUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
