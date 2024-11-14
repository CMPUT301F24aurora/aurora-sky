package com.example.lotteryapp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Organizer} class represents an organizer user type with unique functionalities
 * for managing events. This class extends the {@code User} class and implements {@code Serializable}.
 * It includes functionality for managing event hashes and saving organizer details to Firestore.
 *
 * @see User
 * @see Serializable
 * @see FirebaseFirestore
 * @version v1
 * @since v1
 * @authored by Team Aurora
 */
public class Organizer extends User implements Serializable {

    private List<String> eventHashes;
    private String facility_id;

    public Organizer() {
        this.eventHashes = new ArrayList<>();
    }

    public Organizer(String id, String name, String email) {
        super(id, name, email);
        this.eventHashes = new ArrayList<>();
    }

    public Organizer(String id, String name, String email, String phone) {
        super(id, name, email, phone);
        this.eventHashes = new ArrayList<>();
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Organizer Info: " + getName());
    }

    public void addEventHash(String eventHash, AddEventCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference organizerRef = db.collection("organizers").document(getId());

        eventHashes.add(eventHash);

        organizerRef.update("eventHashes", eventHashes)
                .addOnSuccessListener(aVoid -> callback.onEventAdded(eventHash))
                .addOnFailureListener(e -> {
                    eventHashes.remove(eventHash);
                    callback.onError(e);
                });
    }

    /**
     * Removes an event hash from the organizer's list of events.
     *
     * @param eventHash the hash of the event to be removed
     */
    public void removeEventHash(String eventHash) {
        eventHashes.remove(eventHash);
    }

    public List<String> getEventHashes() {
        return eventHashes;
    }

    public void setEventHashes(List<String> eventHashes) {
        this.eventHashes = eventHashes;
    }

    public String getFacility_id() {
        return this.facility_id;
    }

    public void setFacility_id(String facility_id) {
        this.facility_id = facility_id;
    }

    /**
     * Retrieves an organizer by device ID from Firestore.
     *
     * @param deviceId the device ID to query
     * @param callback callback to handle the result of the query
     * @see GetOrganizerCallback
     */
    public static void getOrganizerByDeviceId(String deviceId, GetOrganizerCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("organizers").whereEqualTo("id", deviceId);

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        Organizer organizer = doc.toObject(Organizer.class);
                        callback.onOrganizerFound(organizer);
                    } else {
                        callback.onOrganizerNotFound();
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void saveToFirestore(SaveOrganizerCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference organizerRef = db.collection("organizers").document(getId());

        organizerRef.set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface AddEventCallback {
        void onEventAdded(String eventHash);
        void onError(Exception e);
    }
}