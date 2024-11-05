package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import java.io.Serializable;

public class Entrant extends User implements Serializable {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Default constructor required for Firestore
    public Entrant() {
    }

    // Constructor with parameters
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    // Constructor with parameters
    public Entrant(String id, String name, String email, String phone) {
        super(id, name, email, phone);
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
                .addOnFailureListener(e -> callback.onError(e));
    }


    // Save Entrant object to Firestore
    public void saveToFirestore(SaveEntrantCallback callback) {
        db.collection("entrants").document(this.getId())
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }
}
