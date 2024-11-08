package com.example.lotteryapp;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Organizer extends User implements Serializable {

    private List<String> eventHashes;

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

    public boolean hasOrganizerPermissions() {
        return "organizer".equals(getRole());
    }

    public void addEventHash(String eventHash, AddEventCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference organizerRef = db.collection("organizers").document(getId());

        // Add the event hash to the local list
        eventHashes.add(eventHash);

        // Update the Firestore document with the new list of event hashes
        organizerRef.update("eventHashes", eventHashes)
                .addOnSuccessListener(aVoid -> {
                    callback.onEventAdded(eventHash);
                })
                .addOnFailureListener(e -> {
                    // If the update fails, remove the event hash from the local list
                    eventHashes.remove(eventHash);
                    callback.onError(e);
                });
    }

    // Remove an event hash from the list
    public void removeEventHash(String eventHash) {
        eventHashes.remove(eventHash);
    }

    // Get the list of event hashes
    public List<String> getEventHashes() {
        return eventHashes;
    }

    public void setEventHashes(List<String> eventHashes) {
        this.eventHashes = eventHashes;
    }

    // Static method to get the Organizer based on device ID
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

    // Callback interface for addEventHash method
    public interface AddEventCallback {
        void onEventAdded(String eventHash);
        void onError(Exception e);
    }

    // Interface for GetOrganizerCallback
//    public interface GetOrganizerCallback {
//        void onOrganizerFound(Organizer organizer);
//        void onOrganizerNotFound();
//        void onError(Exception e);
//    }

    // Interface for SaveOrganizerCallback
//    public interface SaveOrganizerCallback {
//        void onSuccess();
//        void onFailure(Exception e);
//    }
}