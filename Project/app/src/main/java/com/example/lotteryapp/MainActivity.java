package com.example.lotteryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Button for Entrant
        Button entrantButton = findViewById(R.id.entrantButton);
        entrantButton.setOnClickListener(v -> {
            checkEntrantExistsAndNavigate();
        });

        // Button for Organizer
        Button organizerButton = findViewById(R.id.organizerButton);
        organizerButton.setOnClickListener(v -> {
            checkOrganizerStatusAndNavigate();
        });

        // Call the method to check admin status after setting up the UI
        String deviceId = getDeviceId(this);
        checkAdminAndDisplayPage(deviceId);
    }

    private void checkEntrantExistsAndNavigate() {
        // Get the device's unique ID
        String deviceId = getDeviceId(this);

        Entrant.checkEntrantExists(deviceId, new EntrantCheckCallback() {
            @Override
            public void onEntrantExists(Entrant entrant) {
                // If recognized as an entrant, ensure they are also in the organizers collection
                ensureUserInOrganizers(entrant, deviceId);

                // Pass the entrant object to the next activity
                Intent intent = new Intent(MainActivity.this, EntrantsEventsActivity.class);
                intent.putExtra("entrant_data", entrant);  // Pass the entrant object
                startActivity(intent);
            }

            @Override
            public void onEntrantNotFound() {
                Log.d(TAG, "Entrant not found. Navigating to profile edit.");
                // Start the EntrantProfileEditActivity when no entrant is found
                Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Error checking entrant in Firestore", e);
            }
        });
    }

    private void checkOrganizerStatusAndNavigate() {
        String deviceId = getDeviceId(this);
        Log.d(TAG, "Checking in organizers collection for device ID: " + deviceId);

        Entrant.checkEntrantExists(deviceId, new EntrantCheckCallback() {
            @Override
            public void onEntrantExists(Entrant entrant) {
                // If recognized as an entrant, proceed to check or add them to organizers collection
                ensureUserInOrganizers(entrant, deviceId);

                // Proceed to OrganizerMainPage
                Intent intent = new Intent(MainActivity.this, OrganizerMainPage.class);
                startActivity(intent);
            }

            @Override
            public void onEntrantNotFound() {
                // If the user is not recognized, go to OrganizerRegistrationActivity
                Intent intent = new Intent(MainActivity.this, OrganizerRegistrationActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "Error checking entrant in Firestore", e);
            }
        });

        Log.i("Device ID", deviceId);
    }

    // Method to ensure the user is in the organizers collection if they are an entrant
    private void ensureUserInOrganizers(Entrant entrant, String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizers").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Add the user to organizers if they are not there already
                        Log.d(TAG, "User is not in organizers collection. Adding them.");

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", entrant.getName());
                        userData.put("email", entrant.getEmail());
                        userData.put("phone", entrant.getPhone());

                        db.collection("organizers").document(deviceId).set(userData)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully added to organizers collection."))
                                .addOnFailureListener(e -> Log.e(TAG, "Error adding user to organizers collection", e));
                    }
                });
    }


    private void ensureUserInEntrants(Organizer organizer, String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entrants").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Add the user to entrants if they are not there already
                        Log.d(TAG, "User is not in entrants collection. Adding them.");

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", organizer.getName());
                        userData.put("email", organizer.getEmail());
                        userData.put("phone", organizer.getPhone());

                        db.collection("entrants").document(deviceId).set(userData)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully added to entrants collection."))
                                .addOnFailureListener(e -> Log.e(TAG, "Error adding user to entrants collection", e));
                    }
                });
    }


    private void checkAdminAndDisplayPage(String deviceId) {
        // Get the device's unique ID
        db.collection("admin")
                .whereEqualTo("id", deviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Button adminSignInButton = findViewById(R.id.admin_link);

                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Device ID is registered as an admin, show the admin login button
                            adminSignInButton.setVisibility(View.VISIBLE);
                            // Set onClickListener to navigate to admin homepage
                            adminSignInButton.setOnClickListener(v -> navigateToAdminHomepage());
                        } else {
                            // Device ID is not registered as an admin, ensure the button is hidden
                            adminSignInButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void navigateToAdminHomepage() {
        Intent intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
        startActivity(intent);
    }

    private String getDeviceId(Context context) {
        // Get the unique device ID (ANDROID_ID)
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
