package com.example.lotteryapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrganizerCreateEvent extends AppCompatActivity {

    private static final String TAG = "OrganizerCreateEvent";

    private EditText eventDateTime, eventName, eventNumberOfPeople, eventDescription;
    private Button organizerCreateEvent;
    private Organizer organizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Retrieve the Organizer object from the Intent
        organizer = (Organizer) getIntent().getSerializableExtra("organizer");
        if (organizer == null) {
            Toast.makeText(this, "Error: Organizer data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if organizer data is not available
            return;
        } else {
            //Toast.makeText(this, "Error: Organizer found " + organizer.getName(), Toast.LENGTH_SHORT).show();
        }

        // Initialize EditText and Buttons
        eventName = findViewById(R.id.editTextEventName);
        eventDateTime = findViewById(R.id.editTextDateTime);
        eventNumberOfPeople = findViewById(R.id.editNumberOfMembers);
        eventDescription = findViewById(R.id.editTextEventDescription);
        organizerCreateEvent = findViewById(R.id.buttonCreateEvent);

        // Set click listener for eventDateTime to open date and time picker
        eventDateTime.setOnClickListener(v -> openDateTimePicker());

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

        event.saveToFirestore(new SaveEventCallback() {
            @Override
            public void onSuccess(String eventId) {
                // After the event is successfully saved to Firestore, add its hash to the organizer's list
                //Log.v(TAG, "VERBOSE message");

                String eventHash = event.getQR_code();
                organizer.addEventHash(eventHash, new Organizer.AddEventCallback() {
                    @Override
                    public void onEventAdded(String eventHash) {
                        Toast.makeText(OrganizerCreateEvent.this, "Event created Successfully", Toast.LENGTH_SHORT).show();

                        // Save the updated organizer to Firestore
                        organizer.saveToFirestore(new SaveOrganizerCallback() {
                            @Override
                            public void onSuccess() {
                                // Navigate back to OrganizerMainPage
                                Intent intent = new Intent(OrganizerCreateEvent.this, OrganizerMainPage.class);
                                intent.putExtra("organizer", organizer); // Pass the updated organizer back
                                startActivity(intent);

                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OrganizerCreateEvent.this, "Error saving updated organizer", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error saving updated organizer", e);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(OrganizerCreateEvent.this, "Error adding event hash to organizer", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error adding event hash to organizer", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerCreateEvent.this, "Error saving event", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing document", e);
            }
        });
    }
}