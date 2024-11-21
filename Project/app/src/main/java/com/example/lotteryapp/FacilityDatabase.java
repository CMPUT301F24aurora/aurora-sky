package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code FacilityDatabase} class provides methods to interact with the Firestore database
 * for saving and retrieving Facility objects.
 *
 * @version v1
 * @author Team Aurora
 */
public class FacilityDatabase {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Saves a Facility object to Firestore.
     *
     * @param facility The Facility object to be saved
     * @param callback The callback to handle success or failure
     */


    public static void saveToFirestore(Facility facility, FacilityCallback callback) {
        // Create a Map to store the Facility details
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("name", facility.getName());
        facilityData.put("location", facility.getLocation());
        facilityData.put("startTime", facility.getStartTime());
        facilityData.put("endTime", facility.getEndTime());
        facilityData.put("email", facility.getEmail());
        facilityData.put("organizerId", facility.getOrganizerId());

        // Use predefined ID to set the document
        db.collection("facilities").document(facility.getOrganizerId())
                .set(facilityData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);

    }


    /**
     * Retrieves a facility from Firebase Firestore by the specified facility ID.
     *
     * @param facilityId the ID of the facility to retrieve
     * @param callback   the callback to handle success or failure
     */
    public static void getFacilityById(String facilityId, GetFacilityCallback callback) {
        db.collection("facilities").document(facilityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Facility facility = documentSnapshot.toObject(Facility.class);
                    if (facility != null) {
                        facility.setOrganizerId(documentSnapshot.getId());
                        callback.onSuccess(facility);
                    } else {
                        callback.onFailure(new Exception("Facility not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface FacilityCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface GetFacilityCallback {
        void onSuccess(Facility facility);
        void onFailure(Exception e);
    }
}