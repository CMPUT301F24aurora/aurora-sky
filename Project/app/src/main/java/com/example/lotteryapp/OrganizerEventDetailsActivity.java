package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


        // Get event data from the intent
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        Event selectedEvent = (Event) getIntent().getSerializableExtra("event_data");
        String eventId = selectedEvent.getQR_code();

        if (event != null) {
            // Populate views with event details
            eventTitleTextView.setText(event.getEventName());
            eventDateTextView.setText("Date: " + event.getEventDate());
            eventDescriptionTextView.setText(event.getDescription());

            viewEntrantsButton.setOnClickListener(v -> {
                Intent entrantsIntent = new Intent(OrganizerEventDetailsActivity.this, Sampling.class);
                entrantsIntent.putExtra("event_data", event);
                //entrantsIntent.putExtra("event_data", selectedEvent);
                entrantsIntent.putExtra("eventId", eventId);
//                entrantsIntent.putExtra("eventId", entrant.get);
                startActivity(entrantsIntent);

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
        } else {
            // Log error or show a message
            Log.e("OrganizerEventDetails", "Selected event is null!");
            finish(); // Close the activity
        }
    }
}
