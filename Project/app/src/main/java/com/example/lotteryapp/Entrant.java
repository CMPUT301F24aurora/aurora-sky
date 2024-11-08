package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;
import android.content.Context;
import android.provider.Settings;
import android.util.Patterns;

import java.util.Objects;

public class Entrant extends User implements Serializable {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String profileImageUrl;

    // Default constructor required for Firestore
    public Entrant() {}

    // Constructor with three parameters, calls main constructor with a default phone value
    public Entrant(String id, String name, String email, String profileImageUrl) {
        this(id, name, email, null, profileImageUrl);
    }

    // Main constructor with four parameters
    public Entrant(String id, String name, String email, String phone, String profileImageUrl) {
        super(id, name, email, phone);
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Method to remove profile picture
    public void removeProfileImage() {
        this.profileImageUrl = null;
    }

    // Method to inject Firestore instance for testing or alternative setups
    public static void setDatabase(FirebaseFirestore firestore) {
        db = firestore;
    }

    // Method to retrieve the device ID
    private static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // Method to check if an entrant exists in Firestore
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

    // Retrieve Entrant by device ID from Firestore
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

    // Save Entrant object to Firestore
    public void saveToFirestore(SaveEntrantCallback callback) {
        if (callback == null) return;

        db.collection("entrants").document(this.getId())
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    // Validation method for email
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validation method for phone number
    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^[0-9]{10,15}$"; // Allows numbers between 10 and 15 digits
        return phone != null && phone.matches(phoneRegex);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }

    // Override equals to check equality based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;
        Entrant entrant = (Entrant) o;
        return Objects.equals(getId(), entrant.getId());
    }

    // Override hashCode based on ID
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
