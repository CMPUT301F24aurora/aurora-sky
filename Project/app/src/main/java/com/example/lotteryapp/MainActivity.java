package com.example.lotteryapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private GoogleMap myMap;
    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

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
            // Start the Organizer Main Page when the organizer button is clicked
            Intent intent = new Intent(MainActivity.this, OrganizerMainPage.class);
            startActivity(intent);
        });
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

    private String getDeviceId(Context context) {
        // Get the unique device ID (ANDROID_ID)
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
