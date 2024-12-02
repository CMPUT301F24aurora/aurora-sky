package com.example.lotteryapp;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages database operations for the Lottery App using Firebase Firestore.
 */
public class DatabaseManager {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sets a different Firestore instance (for testing purposes).
     *
     * @param firestore The Firestore instance to be used.
     */
    public static void setDatabase(FirebaseFirestore firestore) {
        db = firestore;
    }

    /**
     * Checks if an entrant exists in the database.
     *
     * @param deviceId The device ID of the entrant.
     * @param callback The callback to handle the result.
     */
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

    /**
     * Retrieves an entrant from the database.
     *
     * @param context The context of the application.
     * @param callback The callback to handle the result.
     */
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

    /**
     * Saves an entrant to the database.
     *
     * @param entrant The entrant to be saved.
     * @param callback The callback to handle the result.
     */
    public static void saveEntrant(Entrant entrant, SaveEntrantCallback callback) {
        if (callback == null) return;

        db.collection("entrants").document(entrant.getId())
                .set(entrant)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves an organizer from the database by device ID.
     *
     * @param deviceId The device ID of the organizer.
     * @param callback The callback to handle the result.
     */
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

    /**
     * Saves an organizer to the database.
     *
     * @param organizer The organizer to be saved.
     * @param callback The callback to handle the result.
     */
    public static void saveOrganizer(Organizer organizer, SaveOrganizerCallback callback) {
        DocumentReference organizerRef = db.collection("organizers").document(organizer.getId());

        organizerRef.set(organizer)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds an event hash to an organizer's list of events.
     *
     * @param organizerId The ID of the organizer.
     * @param eventHash The hash of the event to be added.
     * @param callback The callback to handle the result.
     */
    public static void addEventHashToOrganizer(String organizerId, String eventHash, Organizer.AddEventCallback callback) {
        DocumentReference organizerRef = db.collection("organizers").document(organizerId);

        organizerRef.update("eventHashes", com.google.firebase.firestore.FieldValue.arrayUnion(eventHash))
                .addOnSuccessListener(aVoid -> callback.onEventAdded(eventHash))
                .addOnFailureListener(callback::onError);
    }

    /**
     * Fetches entrants from the database by their IDs.
     *
     * @param entrantIds The list of entrant IDs to fetch.
     * @param callback The callback to handle the result.
     */
    public static void fetchEntrantsByIds(List<String> entrantIds, EntrantsFetchCallback callback) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            Log.d("RecyclerListActivity", "No entrants found");
            callback.onFailure("No entrants found");
            return;
        }

        List<Entrant> entrants = new ArrayList<>();

        db.collection("entrants")
                .whereIn("id", entrantIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        Log.d("RecyclerListActivity", "Fetched Entrant: " + entrant.getName());
                        entrants.add(entrant);
                    }
                    callback.onSuccess(entrants);
                })
                .addOnFailureListener(e -> {
                    Log.e("RecyclerListActivity", "Error fetching entrants: ", e);
                    callback.onFailure("Failed to fetch entrants: " + e.getMessage());
                });
    }

    /**
     * Retrieves the device ID.
     *
     * @param context The context of the application.
     * @return The device ID.
     */
    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Callback interface for fetching entrants.
     */
    public interface EntrantsFetchCallback {
        /**
         * Called when entrants are successfully fetched.
         *
         * @param entrants The list of fetched entrants.
         */
        void onSuccess(List<Entrant> entrants);

        /**
         * Called when there's an error fetching entrants.
         *
         * @param errorMessage The error message.
         */
        void onFailure(String errorMessage);
    }
}