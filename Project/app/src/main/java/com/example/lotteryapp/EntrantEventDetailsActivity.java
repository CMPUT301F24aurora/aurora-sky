package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Event event;
    private Entrant entrant;
    private Organizer organizer;
    private Button enterWaitingButton;
    private Button leaveWaitingButton;
    private WaitingList waitingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        initializeViews();
        getIntentData();
        displayEventDetails();

        // Initialize waiting list with the event QR code (as ID)
        waitingList = new WaitingList(event.getQR_code());

        setupEnterWaiting();
        setupLeaveWaiting();
    }

    private void initializeViews() {
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        enterWaitingButton = findViewById(R.id.enter_waiting);
        leaveWaitingButton = findViewById(R.id.leave_waiting);
    }

    private void getIntentData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
    }

    private void displayEventDetails() {
        if (event != null) {
            eventTitle.setText(event.getEventName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());
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
}
