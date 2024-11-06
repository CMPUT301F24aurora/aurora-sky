package com.example.lotteryapp;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Context;

public class Admin extends User{
    //private EntrantsOrganizer entrantsOrganizer;
    //private Event eventsFacility;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    @Override
    public void displayUserInfo() {

    }

    // Constructor
    public Admin(Context context) {
        this.context = context;
    }

    // Remove Event
    public void removeEvent(String eventId) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error deleting Event!", Toast.LENGTH_SHORT).show();
                    }
                });
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
}
