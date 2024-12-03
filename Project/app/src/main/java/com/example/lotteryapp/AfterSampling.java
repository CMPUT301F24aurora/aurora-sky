package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity displayed after the sampling process is completed.
 * Provides navigation to different lists of entrants based on their status.
 */
public class AfterSampling extends AppCompatActivity {

    private static final String TAG = "AfterSampling";

    private String eventId;
    private Event event;
    private Organizer organizer;
    private Entrant entrant;
    private Button waitlistButton, selectedButton, cancelledButton, finalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_sampling_list);

        initializeData();
    }

    /**
     * Initializes event, organizer, and entrant data from Intent extras.
     */
    private void initializeData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        eventId = event != null ? event.getQR_code() : null;
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
    }

    /**
     * Sets up button click listeners for navigating to different entrant lists.
     */
    private void setupButtonListeners() {
        waitlistButton = findViewById(R.id.buttonWaitlistEntrants);
        if (event.getWaitingList() == null || event.getWaitingList().isEmpty()){
            waitlistButton.setEnabled(false);
        } else {
            setupButton(waitlistButton, "Entrants in the waitlist", "waitingList");
        }

        selectedButton = findViewById(R.id.buttonSelectedEntrants);
        if(event.getSelectedEntrants() == null || event.getSelectedEntrants().isEmpty()){
            selectedButton.setEnabled(false);
        } else{
            setupButton(selectedButton, "Selected entrants", "selectedEntrants");
        }

        cancelledButton = findViewById(R.id.buttonCancelledEntrants);
        if (event.getCancelledEntrants() == null || event.getCancelledEntrants().isEmpty()){
            cancelledButton.setEnabled(false);
        } else {
            setupButton(cancelledButton, "Cancelled entrants", "cancelledEntrants");
        }

        finalButton = findViewById(R.id.buttonChosenEntrants);
        if (event.getFinalEntrants() == null || event.getFinalEntrants().isEmpty()){
            finalButton.setEnabled(false);
        } else {
            setupButton(finalButton, "Final chosen entrants", "finalEntrants");
        }
    }

    /**
     * Sets up a button with the given ID to navigate to the specified entrant list.
     *
     * @param button    button in the layout.
     * @param title      Title to be displayed in the target activity.
     * @param collection Collection name to pass to the target activity.
     */
    private void setupButton(Button button, String title, String collection) {
        button.setOnClickListener(v -> navigateToRecyclerList(title, collection));
    }

    /**
     * Navigates to the RecyclerListActivity with the specified title and collection.
     *
     * @param title      The title to be displayed in the RecyclerListActivity.
     * @param collection The name of the collection to be displayed.
     */
    private void navigateToRecyclerList(String title, String collection) {
        Intent intent = new Intent(this, RecyclerListActivity.class);
        Log.d(TAG, "Navigating to RecyclerListActivity with collection: " + collection);

        // Adding extra data to the intent
        intent.putExtra("title", title);
        intent.putExtra("collection", collection);
        intent.putExtra("eventId", eventId);
        intent.putExtra("event_data", event);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("entrant_data", entrant);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    /**
     * Refreshes event details by fetching the latest data from the database.
     */
    private void refreshEventDetails() {
        if (event == null || event.getQR_code() == null) {
            Log.w(TAG, "Event or QR code is null, skipping refresh");
            return;
        }

        new DBManagerEvent().getEventByQRCode(event.getQR_code(), new DBManagerEvent.GetEventCallback() {
            @Override
            public void onSuccess(Event updatedEvent) {
                event = updatedEvent;
                setupButtonListeners();
                Log.d(TAG, "Event updated: " + event.getSelectedEntrants().toString());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AfterSampling.this, "Failed to refresh event details", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error refreshing event details: " + e.getMessage(), e);
            }
        });
    }
}
