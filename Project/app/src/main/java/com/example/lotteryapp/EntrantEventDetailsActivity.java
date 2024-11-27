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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.util.List;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "EntrantEventDetails";

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Event event;
    private Entrant entrant;
    private Organizer organizer;
    private Button enterWaitingButton;
    private Button leaveWaitingButton;
    private WaitingList waitingList;
    private boolean signUp;
    private ImageView eventImageView;
    private LocationHelper locationHelper;
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        locationHelper = new LocationHelper(this);
        databaseHelper = new DatabaseHelper(this);



        getIntentData();
        // Log user's current location
        //logCurrentLocation();

        initializeViews();
        displayEventDetails();

        // Initialize waiting list with the event QR code (as ID)
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
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());

            // Load event image using Glide
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
                if(event.getGeolocationRequired()){
                    showJoinConfirmationDialog();
                } else{
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
        waitingList.addEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantEventDetailsActivity.this, "Joined the Waiting list", Toast.LENGTH_SHORT).show();
                addLocation();
                updateButtonStates();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantEventDetailsActivity.this, "Failed to join the Waiting list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addLocation(){
        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(double latitude, double longitude) {
                // Save the location data to Firestore using DatabaseHelper
                databaseHelper.saveEventEntrantLocation(event.getQR_code(), entrant.getId(), latitude, longitude);
                Log.d("Location", "Location saved: Lat = " + latitude + ", Long = " + longitude);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("Location", "Error retrieving location: " + errorMessage);
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
            enterWaitingButton.setText("Already in Waiting List");
            leaveWaitingButton.setEnabled(true);
            leaveWaitingButton.setText("Leave Waiting List");
        } else {
            enterWaitingButton.setEnabled(true);
            enterWaitingButton.setText("Join Waiting List");
            leaveWaitingButton.setEnabled(false);
            leaveWaitingButton.setText("Not in Waiting List");
        }
    }

    private void autoRegisterIfSignUpTrue() {
        if (signUp) {
            if (!event.getWaitingList().contains(entrant.getId())) {
                waitingList.addEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Automatically joined the Waiting list", Toast.LENGTH_SHORT).show();
                        updateButtonStates();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Failed to automatically join the Waiting list", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.d(TAG, "Entrant already in the waiting list, no need to auto-register.");
            }
        }
    }


}
