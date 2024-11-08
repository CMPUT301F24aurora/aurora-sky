package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;
import android.content.Context;
import android.provider.Settings;
import java.util.Objects;

public class Entrant extends User implements Serializable {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Default constructor required for Firestore
    public Entrant() {}

    // Constructor with parameters
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    // Constructor with parameters
    public Entrant(String id, String name, String email, String phone) {
        super(id, name, email, phone);
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
        db.collection("entrants").document(this.getId())
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
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
