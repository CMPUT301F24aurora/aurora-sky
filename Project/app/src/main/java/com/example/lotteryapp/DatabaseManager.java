package com.example.lotteryapp;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    public static void fetchEntrantsByIds(List<String> entrantIds, EntrantsFetchCallback callback) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            Log.d("RecyclerListActivity", "No entrants found");
            callback.onFailure("No entrants found");
            return;
        }

        List<Entrant> entrants = new ArrayList<>();

        // Query Firestore for entrants by their IDs
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants")
                .whereIn("id", entrantIds) // Fetch entrants where the ID matches one of the provided IDs
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);
                        Log.d("RecyclerListActivity", "Fetched Entrant: " + entrant.getName());
                        entrants.add(entrant);
                    }
                    callback.onSuccess(entrants); // Return the list on success
                })
                .addOnFailureListener(e -> {
                    Log.e("RecyclerListActivity", "Error fetching entrants: ", e);
                    callback.onFailure("Failed to fetch entrants: " + e.getMessage()); // Pass error message on failure
                });
    }


    // Utility method
    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // Callback interface to handle success and failure
    public interface EntrantsFetchCallback {
        void onSuccess(List<Entrant> entrants);
        void onFailure(String errorMessage);
    }
}