package com.example.lotteryapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Activity activity;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Constructor for initializing the LocationHelper.
     *
     * @param context The application or activity context.
     * @param activity The activity to handle permission requests.
     */
    public LocationHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Check if location permissions are granted.
     *
     * @return true if permissions are granted; false otherwise.
     */
    public boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request location permissions if they haven't been granted.
     */
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Check if location services are enabled.
     *
     * @return true if location services are enabled; false otherwise.
     */
    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Prompt the user to enable location services.
     */
    public void promptEnableLocation() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * Retrieves the user's current location as a one-time task and passes it to the provided callback.
     *
     * @param locationCallback A callback to handle the location once it's retrieved.
     */
    public void getCurrentLocation(LocationCallback locationCallback) {
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
            return;
        }

        if (!isLocationEnabled()) {
            promptEnableLocation();
            return;
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
     * A simple callback interface to handle location results.
     */
    public interface LocationCallback {
        void onLocationResult(double latitude, double longitude);
        void onError(String errorMessage);
    }
}
