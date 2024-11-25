package com.example.lotteryapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * A helper class for retrieving the user's current location (latitude and longitude).
 * This class abstracts the logic for fetching location, ensuring proper permissions are handled.
 */
public class LocationHelper {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationProviderClient;

    /**
     * Constructor for initializing the LocationHelper.
     *
     * @param context The application or activity context.
     */
    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Retrieves the user's current location as a one-time task and passes it to the provided callback.
     *
     * @param locationCallback A callback to handle the location once it's retrieved.
     */
    public void getCurrentLocation(LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Location permission not granted!");
        }

        fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        locationCallback.onLocationResult(location.getLatitude(), location.getLongitude());
                    } else {
                        locationCallback.onError("Failed to retrieve location");
                    }
                })
                .addOnFailureListener(e -> locationCallback.onError(e.getMessage()));
    }

    /**
     * Removes continuous location updates (not applicable for one-time location requests).
     *
     * @param googleLocationCallback The location callback from Google Play Services to stop updates.
     */
    public void stopLocationUpdates(com.google.android.gms.location.LocationCallback googleLocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(googleLocationCallback);
    }

    /**
     * A simple callback interface to handle location results.
     */
    public interface LocationCallback {
        void onLocationResult(double latitude, double longitude);

        void onError(String errorMessage);
    }
}
