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
            // Start the EntrantProfileActivity when the entrant button is clicked
            Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
            startActivity(intent);
        });

        // Store device ID on the first run
        storeDeviceIdIfFirstRun();
    }

    private void storeDeviceIdIfFirstRun() {
        // Get the device's unique ID
        String deviceId = getDeviceId(this);

        // Check if the device ID is already stored
        db.collection("devices").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Device ID already exists in Firestore: " + deviceId);
                    } else {
                        // Device ID not found, add it to Firestore
                        Log.d(TAG, "Device ID not found. Storing the device ID.");
                        storeDeviceIdInFirestore(deviceId);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error checking device ID in Firestore", e));
    }

    private void storeDeviceIdInFirestore(String deviceId) {
        // Create a map with the device information
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("deviceId", deviceId);
        deviceData.put("firstOpened", System.currentTimeMillis()); // You can add more info if needed

        // Add the device information to Firestore
        db.collection("devices").document(deviceId)
                .set(deviceData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Device ID successfully stored in Firestore!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error storing device ID in Firestore", e));
    }

    private String getDeviceId(Context context) {
        // Get the unique device ID (ANDROID_ID)
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
