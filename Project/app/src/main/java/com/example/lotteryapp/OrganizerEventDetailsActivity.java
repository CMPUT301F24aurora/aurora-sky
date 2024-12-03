package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

/**
 * Activity for displaying and managing details of an event.
 * <p>
 * This activity allows the organizer to view details about the event, including its title, date, description,
 * and available actions such as viewing entrants, QR code, editing the event, and viewing the entrants' locations.
 * </p>
 *
 * @see AppCompatActivity
 * @see Event
 * @see Organizer
 * @see Entrant
 * @see DBManagerEvent
 */
public class OrganizerEventDetailsActivity extends AppCompatActivity {
    private TextView eventTitleTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private Button viewEntrantsButton, viewQrCodeButton, editEventButton, viewEntrantsLocationButton;
    private ImageView eventPosterView;
    private Organizer organizer;
    private Entrant entrant;
    private Event event;

    /**
     * Called when the activity is created.
     * <p>
     * Initializes the views, loads data from the intent, and sets up the event details and button listeners.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down,
     *                           this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}. Otherwise, it is null.
     * @return void
     * @see #initializeViews()
     * @see #loadIntentData()
     * @see #setupEventDetails()
     * @see #setupButtonListeners()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_details);

        initializeViews();
        loadIntentData();

        if (event != null) {
            setupEventDetails();
            setupButtonListeners();
        } else {
            Log.e("OrganizerEventDetails", "Selected event is null!");
            finish(); // Close the activity
        }
    }

    /**
     * Initializes the views for the event details screen.
     * <p>
     * This method sets up the text views for event title, date, description,
     * and the buttons for viewing entrants, QR code, editing the event, and viewing entrants' location.
     * </p>
     *
     * @return void
     * @see #setupEventDetails()
     * @see #setupButtonListeners()
     */
    private void initializeViews() {
        eventTitleTextView = findViewById(R.id.event_title);
        eventDateTextView = findViewById(R.id.event_date);
        eventDescriptionTextView = findViewById(R.id.event_description);
        viewEntrantsButton = findViewById(R.id.view_entrants_button);
        viewQrCodeButton = findViewById(R.id.view_qr_code_button);
        editEventButton = findViewById(R.id.edit_event_button);
        eventPosterView = findViewById(R.id.poster);
        viewEntrantsLocationButton = findViewById(R.id.view_location);
    }

    /**
     * Loads data from the intent, including event, entrant, and organizer details.
     * <p>
     * This method retrieves the event, entrant, and organizer objects from the intent passed to the activity.
     * </p>
     *
     * @return void
     * @see Intent
     * @see Event
     * @see Organizer
     * @see Entrant
     */
    private void loadIntentData() {
        Intent intent = getIntent();
        event = (Event) intent.getSerializableExtra("event_data");
        entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        organizer = (Organizer) intent.getSerializableExtra("organizer_data");
    }

    /**
     * Sets up the event details in the UI.
     * <p>
     * This method populates the text views with the event's name, start date, and description.
     * If the event requires geolocation, the button to view entrants' location is made visible.
     * </p>
     *
     * @return void
     * @see Event
     */
    private void setupEventDetails() {
        eventTitleTextView.setText(event.getEventName());
        eventDateTextView.setText("Date: " + event.getEventStartDate());
        eventDescriptionTextView.setText(event.getDescription());

        if (event.getImage_url() != null && !event.getImage_url().isEmpty()) {
            eventPosterView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(event.getImage_url())
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(eventPosterView);
        } else {
            //eventPosterView.setVisibility(View.GONE);
        }

        if (event.getGeolocationRequired()) {
            viewEntrantsLocationButton.setVisibility(View.VISIBLE);
        } else {
            viewEntrantsLocationButton.setVisibility(View.GONE);
        }
    }


    private void setupButtonListeners() {
        viewEntrantsButton.setOnClickListener(v -> navigateToEntrantsPage());
        viewQrCodeButton.setOnClickListener(v -> navigateToQRCodePage());
        editEventButton.setOnClickListener(v -> navigateToEditEventPage());
        viewEntrantsLocationButton.setOnClickListener(v -> navigateToLocationPage());
    }

    /**
     * Navigates to the page for viewing entrants based on event status.
     * <p>
     * If the event has cancelled entrants or selected entrants, the activity will navigate to a different page based on the event's state.
     * </p>
     *
     * @return void
     * @see Sampling
     * @see AfterSampling
     */
    private void navigateToEntrantsPage() {

        Log.d("", String.valueOf((event.getCancelledEntrants()==null)));
        Intent intent;
        if ((event.getCancelledEntrants() == null || event.getCancelledEntrants().isEmpty()) &&
                (event.getSelectedEntrants() == null || event.getSelectedEntrants().isEmpty()) &&
                    event.getFinalEntrants()==null) {
            intent = new Intent(this, Sampling.class);
        } else {
            intent = new Intent(this, AfterSampling.class);
        }
        passEventDetails(intent);
        startActivity(intent);
    }

    private void navigateToQRCodePage() {
        Intent intent = new Intent(this, qr_code.class);
        passEventDetails(intent);
        startActivity(intent);
    }

    private void navigateToEditEventPage() {
        Intent intent = new Intent(this, OrganizerCreateEvent.class);
        passEventDetails(intent);
        startActivity(intent);
    }

    private void navigateToLocationPage() {
        Intent intent = new Intent(this, MapActivity.class);
        passEventDetails(intent);
        startActivity(intent);
    }

    private void passEventDetails(Intent intent) {
        //intent.putExtra("event_id",event.getQR_code());
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    private void refreshEventDetails() {
        if (event != null && event.getQR_code() != null) {
            new DBManagerEvent().getEventByQRCode(event.getQR_code(), new DBManagerEvent.GetEventCallback() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    event = updatedEvent;
                    setupEventDetails();  // Refresh the UI with updated data
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventDetailsActivity.this, "Failed to refresh event details", Toast.LENGTH_SHORT).show();
                    Log.e("OrganizerEventDetails", "Error refreshing event details: " + e.getMessage());
                }
            });
        }
    }
}
