package com.example.lotteryapp;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class to manage Firestore database operations.
 */
public class DatabaseHelper {

    private final FirebaseFirestore firestore;
    private final String collectionName = "EventEntrantLocations";

    /**
     * Constructor for initializing the DatabaseHelper.
     *
     * @param context The application or activity context.
     */
    public DatabaseHelper(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Saves the event ID, entrant ID, latitude, and longitude under the specified collection.
     *
     * @param eventId   The ID of the event.
     * @param entrantId The ID of the entrant.
     * @param latitude  The latitude of the entrant's location.
     * @param longitude The longitude of the entrant's location.
     */
    public void saveEventEntrantLocation(String eventId, String entrantId, double latitude, double longitude) {
        if (eventId == null || entrantId == null) {
            Log.e("DatabaseHelper", "Event ID or Entrant ID is null. Cannot save data.");
            return;
        }

        // Prepare the data to be saved
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("entrantId", entrantId);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        // Reference path: EventEntrantLocations/eventId/entrants/entrantId
        String documentPath = String.format("%s/%s/entrants/%s", collectionName, eventId, entrantId);

        // Save data to Firestore
        firestore.document(documentPath)
                .set(locationData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DatabaseHelper", "Location data saved successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("DatabaseHelper", "Failed to save location data: " + e.getMessage());
                    }
                });
    }

    /**
     * Retrieves all entrant locations for a specific event.
     *
     * @param eventId  The ID of the event.
     * @param callback The callback to handle the list of entrant locations.
     */
    public void getEntrantLocationsForEvent(String eventId, EntrantLocationsCallback callback) {
        CollectionReference entrantsRef = firestore.collection(collectionName)
                .document(eventId)
                .collection("entrants");

        entrantsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<EntrantLocation> entrantLocations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String entrantId = document.getId();
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");

                        if (latitude != null && longitude != null) {
                            entrantLocations.add(new EntrantLocation(entrantId, latitude, longitude));
                        }
                    }

                    if (!entrantLocations.isEmpty()) {
                        Log.d("DatabaseHelper", "Retrieved " + entrantLocations.size() + " entrants for event: " + eventId);
                        callback.onEntrantLocationsRetrieved(entrantLocations);
                    } else {
                        Log.d("DatabaseHelper", "No entrants found for event: " + eventId);
                        callback.onEntrantLocationsRetrieved(entrantLocations);
                    }
                } else {
                    Log.e("DatabaseHelper", "Error getting documents: ", task.getException());
                    callback.onError("Failed to retrieve entrant locations: " + task.getException().getMessage());
                }
            }
        });
    }

    /**
     * Callback interface to handle entrant location data retrieval.
     */
    public interface EntrantLocationsCallback {
        void onEntrantLocationsRetrieved(List<EntrantLocation> entrantLocations);
        void onError(String errorMessage);
    }

    /**
     * Data class to represent an entrant's location.
     */
    public static class EntrantLocation {
        private final String entrantId;
        private final double latitude;
        private final double longitude;

        public EntrantLocation(String entrantId, double latitude, double longitude) {
            this.entrantId = entrantId;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getEntrantId() {
            return entrantId;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
