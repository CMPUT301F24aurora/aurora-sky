package com.example.lotteryapp;

import android.content.Context;
import android.provider.Settings;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Method to set a different Firestore instance (for testing)
    public static void setDatabase(FirebaseFirestore firestore) {
        db = firestore;
    }

    // Entrant-related methods
    public static void checkEntrantExists(String deviceId, EntrantCheckCallback callback) {
        if (callback == null) return;

        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        callback.onEntrantExists(entrant);
                    } else {
                        callback.onEntrantNotFound();
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getEntrant(Context context, GetEntrantCallback callback) {
        if (callback == null) return;

        String deviceId = getDeviceId(context);

        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        callback.onEntrantFound(entrant);
                    } else {
                        callback.onEntrantNotFound(new Exception("Entrant not found"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public static void saveEntrant(Entrant entrant, SaveEntrantCallback callback) {
        if (callback == null) return;

        db.collection("entrants").document(entrant.getId())
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Organizer-related methods
    public static void getOrganizerByDeviceId(String deviceId, GetOrganizerCallback callback) {
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

    public static void saveOrganizer(Organizer organizer, SaveOrganizerCallback callback) {
        DocumentReference organizerRef = db.collection("organizers").document(organizer.getId());

        organizerRef.set(organizer)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void addEventHashToOrganizer(String organizerId, String eventHash, Organizer.AddEventCallback callback) {
        DocumentReference organizerRef = db.collection("organizers").document(organizerId);

        organizerRef.update("eventHashes", com.google.firebase.firestore.FieldValue.arrayUnion(eventHash))
                .addOnSuccessListener(aVoid -> callback.onEventAdded(eventHash))
                .addOnFailureListener(callback::onError);
    }


    // Utility method
    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}