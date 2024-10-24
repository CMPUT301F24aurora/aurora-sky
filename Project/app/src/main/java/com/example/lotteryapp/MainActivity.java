package com.example.lotteryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
            // Start the OrganizerRegistration when the organizer button is clicked
            Intent intent = new Intent(MainActivity.this, OrganizerRegistration.class);
            startActivity(intent);
        });
    }

    private void checkEntrantExistsAndNavigate() {
        // Get the device's unique ID
        String deviceId = getDeviceId(this);

        // Check if the entrant exists in the entrants collection
        db.collection("entrants").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Entrant found for device ID: " + deviceId);

                        // Retrieve the entrant data from the document
                        Entrant entrant = documentSnapshot.toObject(Entrant.class);

                        if (entrant != null) {
                            // Pass the entrant object to the next activity
                            Intent intent = new Intent(MainActivity.this, EntrantsEventsActivity.class);
                            intent.putExtra("entrant_data", entrant);  // Pass the entrant object
                            startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "Entrant not found. Navigating to profile edit.");
                        // Start the EntrantProfileEditActivity when no entrant is found
                        Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error checking entrant in Firestore", e));
    }

    private String getDeviceId(Context context) {
        // Get the unique device ID (ANDROID_ID)
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
