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
 * A helper class to manage Firestore database operations for event entrant locations.
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
     * Saves the event ID, entrant ID, name, latitude, and longitude under the specified collection.
     *
     * @param eventId   The ID of the event.
     * @param entrantId The ID of the entrant.
     * @param name      The name of the entrant.
     * @param latitude  The latitude of the entrant's location.
     * @param longitude The longitude of the entrant's location.
     */
    public void saveEventEntrantLocation(String eventId, String entrantId, String name, double latitude, double longitude) {
        if (eventId == null || entrantId == null) {
            Log.e("DatabaseHelper", "Event ID or Entrant ID is null. Cannot save data.");
            return;
        }

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("entrantId", entrantId);
        locationData.put("name", name);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        String documentPath = String.format("%s/%s/entrants/%s", collectionName, eventId, entrantId);

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
                        String name = document.getString("name");
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");

                        if (latitude != null && longitude != null) {
                            entrantLocations.add(new EntrantLocation(entrantId, name, latitude, longitude));
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
        /**
         * Called when entrant locations are successfully retrieved.
         *
         * @param entrantLocations List of retrieved entrant locations.
         */
        void onEntrantLocationsRetrieved(List<EntrantLocation> entrantLocations);

        /**
         * Called when an error occurs during entrant location retrieval.
         *
         * @param errorMessage The error message describing the failure.
         */
        void onError(String errorMessage);
    }

    /**
     * Data class to represent an entrant's location.
     */
    public static class EntrantLocation {
        private final String entrantId;
        private final String name;
        private final double latitude;
        private final double longitude;

        /**
         * Constructor for EntrantLocation.
         *
         * @param entrantId The ID of the entrant.
         * @param name      The name of the entrant.
         * @param latitude  The latitude of the entrant's location.
         * @param longitude The longitude of the entrant's location.
         */
        public EntrantLocation(String entrantId, String name, double latitude, double longitude) {
            this.entrantId = entrantId;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * Gets the entrant ID.
         *
         * @return The entrant ID.
         */
        public String getEntrantId() {
            return entrantId;
        }

        /**
         * Gets the entrant name.
         *
         * @return The entrant name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the latitude of the entrant's location.
         *
         * @return The latitude.
         */
        public double getLatitude() {
            return latitude;
        }

        /**
         * Gets the longitude of the entrant's location.
         *
         * @return The longitude.
         */
        public double getLongitude() {
            return longitude;
        }
    }
}