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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrganizerCreateEvent extends AppCompatActivity {

    private static final String TAG = "OrganizerCreateEvent";

    private EditText eventDateTime, eventName, eventNumberOfPeople, eventDescription, registrationDeadline, waitlistCap;
    private Button organizerCreateEvent, buttonRemovePoster;
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

        if (event != null) { // If event data exists, it's an edit operation
            preloadEventData(event);
        }

//        // Set click listener for eventDateTime to open date and time picker
        eventDateTime.setOnClickListener(v -> openDateTimePicker(eventDateTime));
        registrationDeadline.setOnClickListener(v -> openDateTimePicker(registrationDeadline));
        buttonUploadPoster.setOnClickListener(v->selectImage());
        organizerCreateEvent.setOnClickListener(v -> saveEventDetails());

        buttonRemovePoster.setOnClickListener(v -> {
            imageUri = null;  // Clear the image URI
            buttonUploadPoster.setImageResource(R.drawable.ic_upload_icon);  // Reset upload icon
            buttonRemovePoster.setVisibility(View.GONE);  // Hide the button after removal
            Toast.makeText(this, "Poster removed", Toast.LENGTH_SHORT).show();
        });
    }

    private void preloadEventData(Event event) {
        eventName.setText(event.getEventName());
        eventDateTime.setText(event.getEventDate());
        eventNumberOfPeople.setText(String.valueOf(event.getNumPeople()));
        eventDescription.setText(event.getDescription());
        registrationDeadline.setText(event.getRegistrationDeadline());
        waitlistCap.setText(event.getWaitlistCap());
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

        Event event = new Event(name, dateTime, numPeople, description, waitlistCapValue, registrationDeadlineText);
        if(imageUri != null){
            PosterImage posterImage = new PosterImage();
            posterImage.uploadImage(event.getQR_code(), imageUri, new PosterImage.PosterUploadCallback(){
                @Override
                public void onSuccess(String imageUrl) {
                    event.setImage_url(imageUrl); // Set the image URL to the event
                    dbManagerEvent.addEventToDatabase(event);
                    organizer.addEventHash(event.getQR_code(), new Organizer.AddEventCallback() {
                        @Override
                        public void onEventAdded(String eventHash) {
                            Intent intent = new Intent(OrganizerCreateEvent.this, OrganizerMainPage.class);
                            intent.putExtra("organizer_data", organizer);
                            intent.putExtra("entrant_data", entrant);
                            startActivity(intent);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.d(TAG,"Event Hash adding error");
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerCreateEvent.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            dbManagerEvent.addEventToDatabase(event);
            organizer.addEventHash(event.getQR_code(), new Organizer.AddEventCallback() {
                @Override
                public void onEventAdded(String eventHash) {
                    Intent intent = new Intent(OrganizerCreateEvent.this, OrganizerMainPage.class);
                    intent.putExtra("organizer_data", organizer);
                    intent.putExtra("entrant_data", entrant);
                    startActivity(intent);
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG,"Event Hash adding error");
                }
            });
        }

    }
}