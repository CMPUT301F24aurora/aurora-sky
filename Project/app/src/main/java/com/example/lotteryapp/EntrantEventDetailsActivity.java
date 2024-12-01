package com.example.lotteryapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EntrantEventDetails";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Event event;
    private Entrant entrant;
    private Organizer organizer;
    private Button enterWaitingButton, leaveWaitingButton;
    private WaitingList waitingList;
    private boolean signUp;
    private ImageView eventImageView;
    private LocationHelper locationHelper;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        locationHelper = new LocationHelper(this, this);  // Pass both context and activity
        databaseHelper = new DatabaseHelper(this);

        getIntentData();
        initializeViews();
        displayEventDetails();

        waitingList = new WaitingList(event.getQR_code());
        setupEnterWaiting();
        setupLeaveWaiting();
        autoRegisterIfSignUpTrue();
    }

    private void initializeViews() {
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        enterWaitingButton = findViewById(R.id.enter_waiting);
        leaveWaitingButton = findViewById(R.id.leave_waiting);
        eventImageView = findViewById(R.id.event_poster);
    }

    private void getIntentData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        signUp = getIntent().getBooleanExtra("sign_up", false);
    }

    private void displayEventDetails() {
        if (event != null) {
            eventTitle.setText(event.getEventName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventStartDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());

            if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
                eventImageView.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(event.getImage_url())
                        .placeholder(R.drawable.ic_profile_photo)
                        .error(R.drawable.ic_profile_photo)
                        .into(eventImageView);
            } else {
                eventImageView.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupEnterWaiting() {
        updateButtonStates();
        enterWaitingButton.setOnClickListener(view -> {
            if (event.getWaitingList().contains(entrant.getId())) {
                Toast.makeText(this, "Already in the Waiting list", Toast.LENGTH_SHORT).show();
            } else {
                if (event.getGeolocationRequired()) {
                    showJoinConfirmationDialog();
                } else {
                    joinWaitingList();
                }
            }
        });
    }

    private void showJoinConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Join Waiting List")
                .setMessage("Are you sure you want to join the waiting list for this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    joinWaitingList();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void joinWaitingList() {
        // Check if geolocation is required
        if (event.getGeolocationRequired()) {
            // Check if location services are enabled before proceeding
            if (locationHelper.isLocationEnabled()) {
                // If permission is granted, get the location
                if (locationHelper.isLocationPermissionGranted()) {
                    proceedToJoinWaitingList();
                } else {
                    // Request permission if not granted yet
                    locationHelper.requestLocationPermission();
                }
            } else {
                promptEnableLocation(); // If location is not enabled, prompt user
            }
        } else {
            // If no geolocation is required, directly proceed to join the list
            proceedToJoinWaitingList();
        }
    }


    // Proceed to join the waiting list and add location if required
    private void proceedToJoinWaitingList() {
        waitingList.addEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantEventDetailsActivity.this, "Joined the Waiting list", Toast.LENGTH_SHORT).show();
                // Add location only if geolocation is required
                if (event.getGeolocationRequired()) {
                    addLocation();
                }
                updateButtonStates();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantEventDetailsActivity.this, "Failed to join the Waiting list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Prompt user to enable location services
    private void promptEnableLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Please enable location services to join this event's waiting list.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    //locationHelper.openLocationSettings(); // Open location settings screen
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void addLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                // Save the location data to Firestore using DatabaseHelper
                databaseHelper.saveEventEntrantLocation(event.getQR_code(), entrant.getId(), entrant.getName(), latitude, longitude);
                Log.d(TAG, "Location saved: Lat = " + latitude + ", Long = " + longitude);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving location: " + errorMessage);
            }
        });
    }

    private void setupLeaveWaiting() {
        updateButtonStates();
        leaveWaitingButton.setOnClickListener(view -> {
            if (!event.getWaitingList().contains(entrant.getId())) {
                Toast.makeText(this, "Not in the Waiting list", Toast.LENGTH_SHORT).show();
            } else {
                waitingList.removeEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Left the Waiting list", Toast.LENGTH_SHORT).show();
                        updateButtonStates();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Failed to leave the Waiting list", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateButtonStates() {
        if (event.getWaitingList().contains(entrant.getId())) {
            enterWaitingButton.setEnabled(false);
            leaveWaitingButton.setEnabled(true);
        } else {
            enterWaitingButton.setEnabled(true);
            leaveWaitingButton.setEnabled(false);
        }
    }

    private void autoRegisterIfSignUpTrue() {
        if (signUp) {
            joinWaitingList();
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now proceed with adding to waiting list
                joinWaitingList();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
