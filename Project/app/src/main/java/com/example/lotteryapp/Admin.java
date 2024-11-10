package com.example.lotteryapp;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Context;

/**
 * Represents an Admin user who manages events in the application.
 * This class extends the User class and provides functionalities specific to Admin users.
 *
 * @see User
 * @see FirebaseFirestore
 * @see Context
 * @version v1
 *
 * @author Team Aurora
 */
public class Admin extends User {
    // private EntrantsOrganizer entrantsOrganizer;
    // private Event eventsFacility;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    /**
     * Constructor for the Admin class.
     * Initializes the Admin with the specified context.
     *
     * @param context the context in which the Admin operates
     */
    public Admin(Context context) {
        this.context = context;
    }

    /**
     * Displays the user's information.
     * This method is overridden from the User class.
     *
     * @see User#displayUserInfo()
     */
    @Override
    public void displayUserInfo() {
        // Implementation for displaying user info
    }

    /**
     * Removes an event from the database.
     * Deletes the event document identified by the provided eventId from the "events" collection.
     *
     * @param eventId the ID of the event to be removed
     * @throws Exception if an error occurs while deleting the event
     *
     * @see FirebaseFirestore#collection(String)
     * @see FirebaseFirestore#document(String)
     */
    public void removeEvent(String eventId) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error deleting Event!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
/*
    public void removeProfile(String profileId) {
        entrantsOrganizer.deleteProfile(profileId);
    }

        public void removeImage(String imageId) {
        eventsFacility.deleteImage(imageId);
    }

        public void removeQRCode(String qrCodeHash) {
        eventsFacility.deleteQRCode(qrCodeHash);
    }

        // Browse methods
        public List<Event> browseEvents() {
        return eventsFacility.getAllEvents();
    }

        public List<Profile> browseProfiles() {
        return entrantsOrganizer.getAllProfiles();
    }

        public List<Image> browseImages() {
        return eventsFacility.getAllImages();
    }

        // Enforce policies
        public void enforcePolicy(String facilityId) {
        eventsFacility.removeFacilityIfViolatesGuidelines(facilityId);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Admin ID: " + getId());
        System.out.println("Admin Name: " + getName());
        System.out.println("Admin Email: " + getEmail());
    }
 */
