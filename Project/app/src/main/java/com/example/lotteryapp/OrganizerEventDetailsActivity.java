package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerEventDetailsActivity extends AppCompatActivity {
    private TextView eventTitleTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private Button viewEntrantsButton;
    private Button viewQrCodeButton;
    private Button editEvent;
    private Button viewEntrantsLocation;
    private ImageView eventPosterView;
    private Organizer organizer;
    private Entrant entrant;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_details);

        // Initialize views
        eventTitleTextView = findViewById(R.id.event_title);
        eventDateTextView = findViewById(R.id.event_date);
        eventDescriptionTextView = findViewById(R.id.event_description);
        viewEntrantsButton = findViewById(R.id.view_entrants_button);
        viewQrCodeButton = findViewById(R.id.view_qr_code_button);
        editEvent = findViewById(R.id.edit_event_button);
        eventPosterView = findViewById(R.id.poster);
        viewEntrantsLocation = findViewById(R.id.view_location);


        // Get event data from the intent
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        Event selectedEvent = (Event) getIntent().getSerializableExtra("event_data");
        String eventId = selectedEvent.getQR_code();
        Integer eventCapacity = selectedEvent.getNumPeople();
        String eventQrCode = selectedEvent.getQR_code();

        if (event != null) {
            if (event.getGeolocationRequired()){
                viewEntrantsLocation.setVisibility(View.VISIBLE);
                viewEntrantsLocation.setEnabled(true);
            } else {
                viewEntrantsLocation.setVisibility(View.GONE);
                viewEntrantsLocation.setEnabled(false);
            }
            // Populate views with event details
            eventTitleTextView.setText(event.getEventName());
            eventDateTextView.setText("Date: " + event.getEventStartDate());
            eventDescriptionTextView.setText(event.getDescription());

            viewEntrantsButton.setOnClickListener(v -> {
                // Check if cancelledEntrants and selectedEntrants are empty
                if ((event.getCancelledEntrants() == null || event.getCancelledEntrants().isEmpty()) &&
                        (event.getSelectedEntrants()== null || event.getSelectedEntrants().isEmpty())) {
                    // Navigate to Sampling page if both are empty
                    Intent samplingIntent = new Intent(OrganizerEventDetailsActivity.this, Sampling.class);
                    samplingIntent.putExtra("event_data", event);
                    samplingIntent.putExtra("eventId", eventId);
                    samplingIntent.putExtra("eventQrCode", eventQrCode);
                    samplingIntent.putExtra("eventCapacity", String.valueOf(eventCapacity));
                    startActivity(samplingIntent);
                } else {
                    // Navigate to AfterSampling page if any list is not empty
                    Intent afterSamplingIntent = new Intent(OrganizerEventDetailsActivity.this, AfterSampling.class);
                    afterSamplingIntent.putExtra("event_data", event);
                    afterSamplingIntent.putExtra("eventId", eventId);
                    afterSamplingIntent.putExtra("eventQrCode", eventQrCode);
                    afterSamplingIntent.putExtra("eventCapacity", String.valueOf(eventCapacity));
                    startActivity(afterSamplingIntent);
                }
            });


            viewQrCodeButton.setOnClickListener(v->{
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, qr_code.class);
                intent.putExtra("event_data", event);
                intent.putExtra("entrant_data", entrant);
                intent.putExtra("organizer_data", organizer);
                startActivity(intent);
            });
            editEvent.setOnClickListener(v->{
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, OrganizerCreateEvent.class);
                intent.putExtra("event_data", event);
                intent.putExtra("entrant_data", entrant);
                intent.putExtra("organizer_data", organizer);
                startActivity(intent);
            });

            viewEntrantsLocation.setOnClickListener(v->{
                Intent intent = new Intent(OrganizerEventDetailsActivity.this, MapActivity.class);
                intent.putExtra("event_data", event);
                intent.putExtra("entrant_data", entrant);
                intent.putExtra("organizer_data", organizer);
                startActivity(intent);
            });

        } else {
            // Log error or show a message
            Log.e("OrganizerEventDetails", "Selected event is null!");
            finish(); // Close the activity
        }
    }
}