package com.example.lotteryapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrganizerCreateEvent extends AppCompatActivity {

    private static final String TAG = "OrganizerCreateEvent";

    private EditText eventDateTime, eventName, eventNumberOfPeople, eventDescription, registrationDeadline, waitlistCap;
    private Button organizerCreateEvent, buttonRemovePoster;
    private SwitchCompat geo_toggle;
    private ImageButton buttonUploadPoster;
    private Organizer organizer;
    private Entrant entrant;
    private DBManagerEvent dbManagerEvent;
    private Uri imageUri;
    private Event event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Retrieve the Organizer object from the Intent
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        event = (Event) getIntent().getSerializableExtra("event_data");


        if (organizer == null) {
            Toast.makeText(this, "Error: Organizer data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if organizer data is not available
            return;
        } else {
//            Toast.makeText(this, "Error: Organizer found " + organizer.getName(), Toast.LENGTH_SHORT).show();
        }

        dbManagerEvent = new DBManagerEvent();

//        // Initialize EditText and Buttons
        eventName = findViewById(R.id.editTextEventName);
        eventDateTime = findViewById(R.id.editTextDateTime);
        eventNumberOfPeople = findViewById(R.id.editNumberOfMembers);
        eventDescription = findViewById(R.id.editTextEventDescription);
        buttonUploadPoster = findViewById(R.id.buttonUploadPoster);
        buttonRemovePoster = findViewById(R.id.buttonRemovePoster);
        organizerCreateEvent = findViewById(R.id.buttonCreateEvent);
        waitlistCap = findViewById(R.id.editTextWaitlistCap);
        registrationDeadline = findViewById(R.id.editTextRegistrationDeadline);
        geo_toggle = findViewById(R.id.geo_toggle);

        if (event != null) { // If event data exists, it's an edit operation
            preloadEventData(event);
        }

//        // Set click listener for eventDateTime to open date and time picker
        eventDateTime.setOnClickListener(v -> openDateTimePicker(eventDateTime));
        registrationDeadline.setOnClickListener(v -> openDateTimePicker(registrationDeadline));
        buttonUploadPoster.setOnClickListener(v->selectImage());
        organizerCreateEvent.setOnClickListener(v -> saveEventDetails());

        buttonRemovePoster.setOnClickListener(v -> {
            imageUri = null;  // Clear local reference
            buttonUploadPoster.setImageResource(R.drawable.ic_upload_icon);  // Reset upload icon
            buttonRemovePoster.setVisibility(View.GONE);  // Hide remove button
            buttonRemovePoster.setEnabled(false);
            Toast.makeText(this, "Poster removed", Toast.LENGTH_SHORT).show();
        });
    }

    private void deletePosterImage(String imageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Poster deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete poster", e));
    }


    private void preloadEventData(Event event) {
        eventName.setText(event.getEventName());
        eventDateTime.setText(event.getEventDate());
        eventNumberOfPeople.setText(String.valueOf(event.getNumPeople()));
        eventDescription.setText(event.getDescription());
        registrationDeadline.setText(event.getRegistrationDeadline());
        waitlistCap.setText(event.getWaitlistCap());
        geo_toggle.setChecked(event.getGeolocationRequired());
        if (event.getImage_url() != null){
            buttonRemovePoster.setVisibility(View.VISIBLE);
            buttonRemovePoster.setEnabled(true);
        }
        organizerCreateEvent.setText("Save Event");
    }

    private void openDateTimePicker(EditText targetEditText) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        targetEditText.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);

            timePickerDialog.show();
        });
    }

    // Launcher for selecting an image
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    Toast.makeText(this, "Poster Selected: ", Toast.LENGTH_SHORT).show();
                    buttonRemovePoster.setEnabled(true);
                    buttonRemovePoster.setVisibility(View.VISIBLE);
                }
            }
    );


    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveEventDetails() {
        String name = eventName.getText().toString().trim();
        String dateTime = eventDateTime.getText().toString().trim();
        String numofPeople = eventNumberOfPeople.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();
        String registrationDeadlineText = registrationDeadline.getText().toString().trim();
        String waitlistCapText = waitlistCap.getText().toString().trim();
        Boolean geolocation = geo_toggle.isChecked();


        if (name.isEmpty() || dateTime.isEmpty() || numofPeople.isEmpty() || registrationDeadlineText.isEmpty() ||description.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer numPeople;
        try {
            numPeople = Integer.parseInt(numofPeople);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number of people", Toast.LENGTH_SHORT).show();
            return;
        }

        int waitlistCapValue;
        // Parse waitlist cap
        try {
            waitlistCapValue = Integer.parseInt(waitlistCapText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid waitlist cap value", Toast.LENGTH_SHORT).show();
            return;
        }



        // Use the existing event if editing
        if (event != null) {
            event.setEventName(name);
            event.setEventDate(dateTime);
            event.setNumPeople(numPeople);
            event.setDescription(description);
            event.setGeolocationRequired(geolocation);
            event.setRegistrationDeadline(registrationDeadlineText);
            event.setWaitlistCap(waitlistCapValue);
        } else {
            // Create a new event only if it's not an edit operation
            event = new Event(name, dateTime, numPeople, description, waitlistCapValue, registrationDeadlineText, geolocation);
        }

        // Check if there is an image to upload
        if (imageUri != null) {
            PosterImage posterImage = new PosterImage();
            posterImage.uploadImage(event.getQR_code(), imageUri, new PosterImage.PosterUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    event.setImage_url(imageUrl);
                    updateOrCreateEventInDatabase(event);  // Call method to update or create event
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerCreateEvent.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            if (imageUri == null && event.getImage_url() != null) {
                // Delete image from storage and remove URL from the event object
                deletePosterImage(event.getImage_url());
                event.setImage_url(null);
            }
            updateOrCreateEventInDatabase(event);  // Call method to update or create event without image
        }
    }

    // Helper method to handle database operations for both new and edited events
    private void updateOrCreateEventInDatabase(Event event) {
        dbManagerEvent.addEventToDatabase(event);  // Assuming this method handles both update and create logic

        // Skip adding event hash if editing an existing event
        if (getIntent().getSerializableExtra("event_data") == null) {  // Only add hash for new events
            organizer.addEventHash(event.getQR_code(), new Organizer.AddEventCallback() {
                @Override
                public void onEventAdded(String eventHash) {
                    navigateToMainPage();
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "Event Hash adding error");
                }
            });
        } else {
            navigateToMainPage();  // Navigate directly for edits
        }
    }

    private void navigateToMainPage() {
        Intent intent = new Intent(OrganizerCreateEvent.this, OrganizerMainPage.class);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("entrant_data", entrant);
        startActivity(intent);
    }

}