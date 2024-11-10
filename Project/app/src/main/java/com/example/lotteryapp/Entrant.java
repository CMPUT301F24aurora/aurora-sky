package com.example.lotteryapp;

import android.content.Context;
import android.provider.Settings;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Objects;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Entrant class represents an entrant user in the application.
 * This class extends the User class and implements Serializable for data storage and retrieval.
 *
 * @see User
 * @see FirebaseFirestore
 * @see Serializable
 * @version v1
 *
 * Author: Team Aurora
 */
public class Entrant extends User implements Serializable {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String profileImageUrl;

    /**
     * Default constructor required for Firestore.
     */
    public Entrant() {}

    /**
     * Constructor with three parameters, calls main constructor with a default phone value.
     *
     * @param id the ID of the entrant
     * @param name the name of the entrant
     * @param email the email of the entrant
     */
    public Entrant(String id, String name, String email) {}

    /**
     * Constructor with four parameters.
     *
     * @param id the ID of the entrant
     * @param name the name of the entrant
     * @param email the email of the entrant
     * @param profileImageUrl the URL of the entrant's profile image
     */
    public Entrant(String id, String name, String email, String profileImageUrl) {
        this(id, name, email, null, profileImageUrl);
    }

    /**
     * Main constructor with five parameters.
     *
     * @param id the ID of the entrant
     * @param name the name of the entrant
     * @param email the email of the entrant
     * @param phone the phone number of the entrant
     * @param profileImageUrl the URL of the entrant's profile image
     */
    public Entrant(String id, String name, String email, String phone, String profileImageUrl) {
        super(id, name, email, phone);
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the profile image URL of the entrant.
     *
     * @return the profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL of the entrant.
     *
     * @param profileImageUrl the new profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Removes the profile image of the entrant.
     */
    public void removeProfileImage() {
        this.profileImageUrl = null;
    }

    /**
     * Sets the Firestore database instance for testing or alternative setups.
     *
     * @param firestore the Firestore instance to set
     */
    public static void setDatabase(FirebaseFirestore firestore) {
        db = firestore;
    }

    /**
     * Retrieves the device ID.
     *
     * @param context the context to use for retrieving the device ID
     * @return the device ID
     */
    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Checks if an entrant exists in Firestore.
     *
     * @param deviceId the device ID to check for
     * @param callback the callback to handle the result
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
     * Retrieves an entrant by device ID from Firestore.
     *
     * @param context the context to use for retrieving the device ID
     * @param callback the callback to handle the result
     */
    public static void getEntrant(Context context, GetEntrantCallback callback) {
        if (callback == null) return;

        String deviceId = getDeviceId(context); // Get device ID

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
     * Saves the Entrant object to Firestore.
     *
     * @param callback the callback to handle the result
     */
    public void saveToFirestore(SaveEntrantCallback callback) {
        if (callback == null) return;

        db.collection("entrants").document(this.getId())
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Displays the user's information.
     * This method is overridden from the User class.
     */
    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }

    /**
     * Checks if this Entrant is equal to another object.
     * Equality is based on the ID of the Entrant.
     *
     * @param o the object to compare to
     * @return true if the objects are equal, false otherwise)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;
        Entrant entrant = (Entrant) o;
        return Objects.equals(getId(), entrant.getId());
    }

    /**
     * Returns the hash code of this Entrant.
     * The hash code is based on the ID of the Entrant.
     *
     * @return the hash code of this Entrant
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
