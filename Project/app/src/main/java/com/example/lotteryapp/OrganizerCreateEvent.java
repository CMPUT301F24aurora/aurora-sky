package com.example.lotteryapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    private EditText eventDateTime, eventName, eventNumberOfPeople, eventDescription;
    private Button organizerCreateEvent;
    private ImageButton buttonUploadPoster;
    private Organizer organizer;
    private Entrant entrant;
    private DBManagerEvent dbManagerEvent;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Retrieve the Organizer object from the Intent
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");

        if (organizer == null) {
            Toast.makeText(this, "Error: Organizer data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if organizer data is not available
            return;
        } else {
            //Toast.makeText(this, "Error: Organizer found " + organizer.getName(), Toast.LENGTH_SHORT).show();
        }

        dbManagerEvent = new DBManagerEvent();

        // Initialize EditText and Buttons
        eventName = findViewById(R.id.editTextEventName);
        eventDateTime = findViewById(R.id.editTextDateTime);
        eventNumberOfPeople = findViewById(R.id.editNumberOfMembers);
        eventDescription = findViewById(R.id.editTextEventDescription);
        buttonUploadPoster = findViewById(R.id.buttonUploadPoster);
        organizerCreateEvent = findViewById(R.id.buttonCreateEvent);

        // Set click listener for eventDateTime to open date and time picker
        eventDateTime.setOnClickListener(v -> openDateTimePicker());
        buttonUploadPoster.setOnClickListener(v->selectImage());
        organizerCreateEvent.setOnClickListener(v -> saveEventDetails());
    }

    private void openDateTimePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Event Date")
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
                        eventDateTime.setText(sdf.format(calendar.getTime()));
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

        if (name.isEmpty() || dateTime.isEmpty() || numofPeople.isEmpty() || description.isEmpty()) {
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

        Event event = new Event(name, dateTime, numPeople, description);
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