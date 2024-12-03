package com.example.lotteryapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import java.util.Objects;

/**
 * Activity for displaying event details to entrants and managing waiting list operations.
 */
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

    /**
     * Initializes the activity, sets up UI components, and loads event details.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);
        locationHelper = new LocationHelper(this, this);
        databaseHelper = new DatabaseHelper(this);

        getIntentData();
        initializeViews();
        displayEventDetails();
        waitingList = new WaitingList(event.getQR_code());
        setupEnterWaiting();
        setupLeaveWaiting();
        autoRegisterIfSignUpTrue();
    }

    /**
     * Initializes view components.
     */
    private void initializeViews() {
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        enterWaitingButton = findViewById(R.id.enter_waiting);
        leaveWaitingButton = findViewById(R.id.leave_waiting);
        eventImageView = findViewById(R.id.event_poster);
    }

    /**
     * Retrieves data passed through the intent.
     */
    private void getIntentData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        signUp = getIntent().getBooleanExtra("sign_up", false);
        Log.d("", "Signup Value: " + String.valueOf(signUp));

        if (event == null) {
            Log.e(TAG, "Event data is missing from intent.");
        } else {
            Log.d(TAG, "Event retrieved: " + event.getEventName());
        }
        if (entrant == null) {
            Log.e(TAG, "Entrant data is missing from intent.");
        }
    }

    /**
     * Displays event details in the UI.
     */
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

    /**
     * Sets up the enter waiting list button and its click listener.
     */
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

    /**
     * Shows a confirmation dialog for joining the waiting list.
     */
    private void showJoinConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Join Waiting List")
                .setMessage("Are you sure you want to join the waiting list for this event? This event has geolocation requirement!!")
                .setPositiveButton("Yes", (dialog, which) -> {
                    joinWaitingList();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    /**
     * Handles the process of joining the waiting list, including location checks if required.
     */
    private void joinWaitingList() {
        if(event.getWaitlistCap() != -1){
            if (Objects.equals(event.getWaitingListLength(), event.getWaitlistCap())){
                Toast.makeText(this, "Waiting List is full", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (event.getGeolocationRequired()) {
            if (locationHelper.isLocationEnabled()) {
                if (locationHelper.isLocationPermissionGranted()) {
                    proceedToJoinWaitingList();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        new AlertDialog.Builder(this)
                                .setTitle("Location Permission")
                                .setMessage("We need access to your location to add you to the waiting list for this event.")
                                .setPositiveButton("OK", (dialog, which) -> locationHelper.requestLocationPermission())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Location Permission Denied")
                                .setMessage("Location permission is required to join the waiting list. Please enable it in the app settings.")
                                .setPositiveButton("Go to Settings", (dialog, which) -> openAppSettings())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    }
                }
            } else {
                promptEnableLocation();
            }
        } else {
            proceedToJoinWaitingList();
        }
    }

    /**
     * Opens the app settings page.
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Proceeds with joining the waiting list after all checks are passed.
     */
    private void proceedToJoinWaitingList() {
        Log.e("", event.getWaitingList().toString());
        waitingList.addEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantEventDetailsActivity.this, "Joined the Waiting list", Toast.LENGTH_SHORT).show();
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

    /**
     * Prompts the user to enable location services.
     */
    private void promptEnableLocation() {
        new AlertDialog.Builder(this)
                .setTitle("Location Required")
                .setMessage("Please enable location services to join this event's waiting list.")
                .setPositiveButton("Enable", (dialog, which) -> {
                    // Open location settings screen
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Adds the entrant's location to the database.
     */
    private void addLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                databaseHelper.saveEventEntrantLocation(event.getQR_code(), entrant.getId(), entrant.getName(), latitude, longitude);
                Log.d(TAG, "Location saved: Lat = " + latitude + ", Long = " + longitude);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error retrieving location: " + errorMessage);
            }
        });
    }

    /**
     * Sets up the leave waiting list button and its click listener.
     */
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

    /**
     * Updates the states of the enter and leave waiting list buttons.
     */
    private void updateButtonStates() {
        if (event.getWaitingList().contains(entrant.getId())) {
            enterWaitingButton.setEnabled(false);
            leaveWaitingButton.setEnabled(true);
        } else {
            enterWaitingButton.setEnabled(true);
            leaveWaitingButton.setEnabled(false);
        }
    }

    /**
     * Automatically registers the entrant if sign-up is true.
     */
    private void autoRegisterIfSignUpTrue() {
        if (signUp) {
            Log.d(TAG, "Preparing to auto sign up");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing() && !isDestroyed()) {
                        Log.d(TAG, "Attempting auto sign up");
                        enterWaitingButton.performClick();
                    }
                }
            }, 500); // 500 milliseconds delay
        }
    }

    /**
     * Refreshes event details when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    /**
     * Refreshes the event details from the database.
     */
    private void refreshEventDetails() {
        if (event != null && event.getQR_code() != null) {
            DBManagerEvent dbManagerEvent = new DBManagerEvent();
            dbManagerEvent.getEventByQRCode(event.getQR_code(), new DBManagerEvent.GetEventCallback() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    event = updatedEvent;
                    displayEventDetails();
                    updateButtonStates();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EntrantEventDetailsActivity.this, "Failed to refresh event details", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error refreshing event details: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Handles the result of the location permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                joinWaitingList();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}