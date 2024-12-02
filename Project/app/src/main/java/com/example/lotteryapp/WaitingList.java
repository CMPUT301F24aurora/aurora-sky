package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The {@code WaitingList} class represents a waiting list for a specific event.
 * It manages entrants and enforces a cap on the number of entrants, if applicable.
 * Updates are synchronized with a Firestore database.
 *
 * @see FirebaseFirestore
 * @see OnDatabaseUpdateListener
 */
public class WaitingList {
    private String eventId;  // The ID of the event this waiting list belongs to
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Integer waitlistCap;

    public WaitingList(String eventId){
        this.eventId = eventId;
        fetchWaitlistCap();
    }

    /**
     * Fetches the waitlist capacity from the Firestore database.
     * If the capacity is not set or if an error occurs during retrieval,
     * the {@code waitlistCap} is set to {@code null}, indicating no cap.
     *
     * @throws IllegalStateException if Firestore access fails
     */
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

    /**
     * Adds an entrant to the waiting list if the entrant is not already present and the waitlist is not full.
     *
     * @param entrantId the unique identifier of the entrant to be added
     * @param waitingListIds the current list of entrant IDs in the waitlist
     * @param listener a callback interface to notify the result of the database update
     * @return {@code true} if the entrant was successfully added; {@code false} otherwise
     * @throws IllegalStateException if Firestore access fails during database update
     * @see OnDatabaseUpdateListener
     */
    // Add or remove methods remain unchanged, but include callbacks for database operations
    public boolean addEntrant(String entrantId, List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        if (waitlistCap != -1 && waitingListIds.size() >= waitlistCap) {
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

    /**
     * Removes an entrant from the waiting list if the entrant is present.
     *
     * @param entrantId the unique identifier of the entrant to be removed
     * @param waitingListIds the current list of entrant IDs in the waitlist
     * @param listener a callback interface to notify the result of the database update
     * @return {@code true} if the entrant was successfully removed; {@code false} otherwise
     * @throws IllegalStateException if Firestore access fails during database update
     * @see OnDatabaseUpdateListener
     */
    public boolean removeEntrant(String entrantId, List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        if (waitingListIds.contains(entrantId)) {
            waitingListIds.remove(entrantId);
            updateDatabase( waitingListIds, listener); // Update Firestore with a callback
            return true;
        }
        return false;
    }

    /**
     * Updates the Firestore database with the current state of the waiting list.
     *
     * @param waitingListIds the updated list of entrant IDs in the waitlist
     * @param listener a callback interface to notify the result of the update
     * @throws IllegalStateException if Firestore access fails during update
     * @see FirebaseFirestore
     */
    private void updateDatabase(List<String> waitingListIds, OnDatabaseUpdateListener listener) {
        db.collection("events").document(eventId)
                .update("waitingList", waitingListIds)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e));
    }

    /**
     * Callback interface for notifying the results of database update operations.
     */
    public interface OnDatabaseUpdateListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}