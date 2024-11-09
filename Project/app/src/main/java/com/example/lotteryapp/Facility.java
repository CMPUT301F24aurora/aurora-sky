package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.io.Serializable;
/**
 * The {@code Facility} class represents a facility managed by an organizer.
 * It includes details such as the organizer ID, facility name, operating hours, location, and contact email.
 * This class allows saving, updating, and deleting facility information in Firebase Firestore.
 *
 * @see FirebaseFirestore
 * @see FacilityCallback
 * @see GetFacilityCallback
 * @version v1
 * @author Team Aurora
 */
public class Facility implements Serializable {

    private String organizerId;
    private String name;
    private String startTime;
    private String endTime;
    private String location;
    private String email;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Facility() {}

    public Facility(String organizerId, String name, String startTime, String endTime, String location, String email) {
        this.organizerId = organizerId;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.email = email;
    }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void saveToFirestore(FacilityCallback callback) {
        if (organizerId == null || organizerId.isEmpty()) {
            // Create a new document with an auto-generated ID
            db.collection("facilities")
                    .add(this)
                    .addOnSuccessListener(documentReference -> {
                        this.organizerId = documentReference.getId();
                        callback.onSuccess();
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            // Update an existing document with the provided ID
            db.collection("facilities").document(this.organizerId)
                    .set(this)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onFailure);
        }
    }

    public void updateInFirestore(FacilityCallback callback) {
        db.collection("facilities").document(this.organizerId)
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteFromFirestore(FacilityCallback callback) {
        db.collection("facilities").document(this.organizerId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves a facility from Firebase Firestore by the specified facility ID.
     *
     * @param facilityId the ID of the facility to retrieve
     * @param callback   the callback to handle success or failure
     * @throws Exception if the facility is not found
     * @see GetFacilityCallback
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