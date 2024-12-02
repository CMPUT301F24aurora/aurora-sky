package com.example.lotteryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * The {@code MapActivity} class is responsible for displaying the user's current location on a Google Map.
 * It handles location permissions, retrieves the last known location, and places a marker on the map at
 * the user's location.
 *
 * This activity also retrieves and displays the locations of entrants for a specific event.
 *
 * @see AppCompatActivity
 * @see OnMapReadyCallback
 * @see GoogleMap
 * @see FusedLocationProviderClient
 * @version v1
 * @since v1
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    /** The Google Map object used to display the map. */
    private GoogleMap mMap;

    /** The FusedLocationProviderClient used to get the user's location. */
    private FusedLocationProviderClient fusedLocationProviderClient;

    /** The request code for fine location permission. */
    private static final int FINE_PERMISSION_CODE = 1;

    /** Tag for logging purposes. */
    private static final String TAG = "MapActivity";

    /** Helper class for database operations. */
    private DatabaseHelper databaseHelper;

    /** The event object containing event details. */
    private Event event;

    /** The ID of the event. */
    private String eventId;

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseHelper = new DatabaseHelper(this);
        event = (Event) getIntent().getSerializableExtra("event_data");
        eventId = event.getQR_code();

        // Check and request location permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            } else {
                initializeMap(); // Initialize map if permission is already granted
            }
        } else {
            initializeMap(); // Initialize map for older versions
        }
    }

    /**
     * Initializes the map by getting the SupportMapFragment and requesting the GoogleMap object.
     */
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     *
     * @param googleMap The GoogleMap object representing the Google Map.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Retrieve entrant locations and add markers
        databaseHelper.getEntrantLocationsForEvent(eventId, new DatabaseHelper.EntrantLocationsCallback() {
            @Override
            public void onEntrantLocationsRetrieved(List<DatabaseHelper.EntrantLocation> entrantLocations) {
                for (DatabaseHelper.EntrantLocation location : entrantLocations) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(position).title("Entrant: " + location.getName()));
                }

                if (!entrantLocations.isEmpty()) {
                    // Move camera to the first entrant's location as an example
                    LatLng firstLocation = new LatLng(entrantLocations.get(0).getLatitude(), entrantLocations.get(0).getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving entrant locations: " + errorMessage);
                Toast.makeText(MapActivity.this, "Failed to load entrant locations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Callback for the result from requesting permissions.
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap(); // Permission granted, setup the map again
            } else {
                Log.w(TAG, "Location permission denied.");
            }
        }
    }
}