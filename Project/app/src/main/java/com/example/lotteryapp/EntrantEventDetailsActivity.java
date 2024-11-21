package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Event event;
    private Entrant entrant;
    private Organizer organizer;
    private Button enterWaitingButton;
    private Button leaveWaitingButton;
    private WaitingList waitingList;
    private boolean signUp;
    private ImageView eventImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        getIntentData();
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
        signUp = (Boolean) getIntent().getBooleanExtra("sign_up", false);
    }

    private void displayEventDetails() {
        if (event != null) {
            eventTitle.setText(event.getEventName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());

            // Check if image URL is available
            if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
                eventImageView.setVisibility(View.VISIBLE);
                // Load the image using Glide
                Glide.with(this)
                        .load(event.getImage_url())
                        .placeholder(R.drawable.ic_profile_photo) 
                        .error(R.drawable.ic_profile_photo)
                        .into(eventImageView);
            } else {
                // Hide ImageView if there's no image URL
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
                waitingList.addEntrant(entrant.getId(), event.getWaitingList(), new WaitingList.OnDatabaseUpdateListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Joined the Waiting list", Toast.LENGTH_SHORT).show();
                        updateButtonStates();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantEventDetailsActivity.this, "Failed to join the Waiting list", Toast.LENGTH_SHORT).show();
                    }
                });
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

    /**
     * Updates the states of the Enter and Leave Waiting buttons
     * based on whether the entrant is already in the waiting list.
     */
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
                Log.d("EntrantEventDetails", "Entrant already in the waiting list, no need to auto-register.");
            }
        }
    }
}
