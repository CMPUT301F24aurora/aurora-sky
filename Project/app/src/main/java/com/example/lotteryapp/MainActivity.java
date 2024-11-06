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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class  MainActivity extends AppCompatActivity {

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
    }

    private void checkOrganizerStatusAndNavigate() {
        String deviceId = getDeviceId(this);
        Log.d(TAG, "Checking in organizers collection for device ID: " + deviceId);

        Entrant.checkEntrantExists(deviceId, new EntrantCheckCallback() {
            @Override
            public void onEntrantExists(Entrant entrant) {
                // If the user is recognized as an entrant, proceed to OrganizerMainPage
                Log.d(TAG, "User recognized as an entrant.");

                // Now check if they exist in the 'organizers' collection
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("organizers").document(deviceId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                // If the user is not an organizer, add them to the 'organizers' collection
                                Log.d(TAG, "User is not in organizers collection. Adding them.");
                                Map<String, Object> organizerData = new HashMap<>();
                                organizerData.put("name", entrant.getName());
                                organizerData.put("email", entrant.getEmail());
                                organizerData.put("phone", entrant.getPhone());

                                db.collection("organizers")
                                        .document(deviceId) // Use deviceId as the document ID
                                        .set(organizerData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "User successfully added to organizers collection.");
                                            // Proceed to OrganizerMainPage
                                            Intent intent = new Intent(MainActivity.this, OrganizerMainPage.class);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error adding user to organizers collection", e);
                                            Toast.makeText(MainActivity.this, "Error registering as organizer", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // User is already an organizer, proceed to OrganizerMainPage
                                Log.d(TAG, "User is already an organizer.");
                                Intent intent = new Intent(MainActivity.this, OrganizerMainPage.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error checking organizers collection", e);
                            Toast.makeText(MainActivity.this, "Error checking organizers", Toast.LENGTH_SHORT).show();
                        });
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

        //Print it for our reference to add to the admin list
        Log.i("Device ID", deviceId);

        // Button for signing in as Admin
        Button adminSignInButton = findViewById(R.id.admin_link);
        adminSignInButton.setVisibility(View.GONE);
        checkAdminAndDisplayPage(deviceId);

        Event event = new Event("Dance Class", "21/10/2024", 40, "Dancey Dance");
//        event.saveToFirestore(new Event.SaveEventCallback() {
//
//            @Override
//            public void onSuccess(String documentId) {
//                // Call the function to generate a unique qr code using the unique event document ID
//                String QrHash = event.generateQRHash(documentId);
//                // Update the event with the newly generated QR hash
//                DocumentReference docRef = db.collection("events").document(documentId);
//                docRef.update("QR Hash", QrHash)
//                        .addOnSuccessListener(aVoid -> {
//                            System.out.println("QR Hash added successfully!");
//                        })
//                        .addOnFailureListener(e -> {
//                            System.out.println("Error updating document: " + e.getMessage());
//                        });
//                System.out.println("Event saved successfully with Document ID: " + documentId);
//                System.out.println("Event created successfully with QR Hash: " + QrHash);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                System.out.println("Failed to save event: " + e.getMessage());
//            }
//        });

    }
    private void checkEntrantExistsAndNavigate() {
        // Get the device's unique ID
        String deviceId = getDeviceId(this);

        Entrant.checkEntrantExists(deviceId, new EntrantCheckCallback() {
            @Override
            public void onEntrantExists(Entrant entrant) {
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

    private void checkAdminAndDisplayPage(String deviceId) {
        // Get the device's unique ID
        db.collection("admin")
                .whereEqualTo("id", deviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Device ID is registered as an admin, show the admin login button
                            Button adminSignInButton = findViewById(R.id.admin_link);
                            adminSignInButton.setVisibility(View.VISIBLE);
                            // Set onClickListener to navigate to admin homepage
                            adminSignInButton.setOnClickListener(new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    navigateToAdminHomepage();
                                }
                            });
                        }
                        else {
                            // Device ID is not registered as an admin
                            //Toast.makeText(MainActivity.this, "Access Denied, not an Admin!", Toast.LENGTH_SHORT).show();
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
