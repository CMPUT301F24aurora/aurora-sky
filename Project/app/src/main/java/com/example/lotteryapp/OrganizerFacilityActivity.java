package com.example.lotteryapp;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class OrganizerFacilityActivity extends AppCompatActivity {

    private EditText nameField, locationField, emailField;
    private Button startTimeButton, endTimeButton, saveButton, removeButton;
    private Facility facility;
    private boolean isEditing;
    private Organizer organizer;
    private Entrant entrant;
    private FacilityDatabase facilityDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_facility);

        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        entrant = (Entrant)  getIntent().getSerializableExtra("entrant_data");
        if (organizer == null) {
            Toast.makeText(this, "Error: Organizer data not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if organizer data is not available
            return;
        }

        facilityDatabase = new FacilityDatabase();

        initializeViews();
        setupFacilityData();
        setupTimeButtons();
        setupSaveButton();
    }

    private void initializeViews() {
        nameField = findViewById(R.id.name_field);
        startTimeButton = findViewById(R.id.start_time_button);
        endTimeButton = findViewById(R.id.end_time_button);
        locationField = findViewById(R.id.location_field);
        emailField = findViewById(R.id.email_field);
        saveButton = findViewById(R.id.save_button);
        removeButton = findViewById(R.id.remove_button);
    }

    private void setupFacilityData() {
        String facilityId = organizer.getFacility_id();
        if (facilityId != null && !facilityId.isEmpty()) {
            // Load existing facility data
            loadFacilityData(facilityId);
        } else {
            // Create new facility
            facility = new Facility();
            facility.setOrganizerId(organizer.getId());
            isEditing = false;
            removeButton.setVisibility(View.GONE);
        }
    }

    private void loadFacilityData(String facilityId) {
        facilityDatabase.getFacilityById(facilityId, new FacilityDatabase.GetFacilityCallback() {
            @Override
            public void onSuccess(Facility loadedFacility) {
                facility = loadedFacility;
                isEditing = true;
                populateFacilityData();
                removeButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerFacilityActivity.this, "Failed to load facility data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFacilityData() {
        nameField.setText(facility.getName());
        startTimeButton.setText("Start Time: " + facility.getStartTime());
        endTimeButton.setText("End Time: " + facility.getEndTime());
        locationField.setText(facility.getLocation());
        emailField.setText(facility.getEmail());
    }

    private void setupTimeButtons() {
        startTimeButton.setOnClickListener(v -> showTimePicker(true));
        endTimeButton.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(final boolean isStartTime) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(OrganizerFacilityActivity.this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    if (isStartTime) {
                        startTimeButton.setText("Start Time: " + time);
                        facility.setStartTime(time);
                    } else {
                        endTimeButton.setText("End Time: " + time);
                        facility.setEndTime(time);
                    }
                }, hour, minute, true);

        timePicker.show();
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateFacilityData();
                saveFacilityToFirestore();
            }
        });
    }

    private boolean validateInputs() {
        String name = nameField.getText().toString().trim();
        String location = locationField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String startTimeText = startTimeButton.getText().toString().replace("Start Time: ", "").trim();
        String endTimeText = endTimeButton.getText().toString().replace("End Time: ", "").trim();

        if (name.isEmpty() || location.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name, location, and email are required.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startTimeText.isEmpty() || endTimeText.isEmpty()) {
            Toast.makeText(this, "Start time and end time are required.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateFacilityData() {
        facility.setName(nameField.getText().toString().trim());
        facility.setLocation(locationField.getText().toString().trim());
        facility.setEmail(emailField.getText().toString().trim());

        String startTimeText = startTimeButton.getText().toString().replace("Start Time: ", "").trim();
        String endTimeText = endTimeButton.getText().toString().replace("End Time: ", "").trim();

        facility.setStartTime(startTimeText);
        facility.setEndTime(endTimeText);
        facility.setOrganizerId(organizer.getId());

    }

    private void saveFacilityToFirestore() {
        Log.d("FacilityUpdate", "Name: " + facility.getName());
        Log.d("FacilityUpdate", "Location: " + facility.getLocation());
        Log.d("FacilityUpdate", "Email: " + facility.getEmail());
        Log.d("FacilityUpdate", "Start Time: " + facility.getStartTime());
        Log.d("FacilityUpdate", "End Time: " + facility.getEndTime());
        Log.d("FacilityUpdate", "Organizer ID: " + facility.getOrganizerId());
        facilityDatabase.saveToFirestore(facility, new FacilityDatabase.FacilityCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(OrganizerFacilityActivity.this, "Facility saved successfully", Toast.LENGTH_SHORT).show();
                organizer.setFacility_id(facility.getOrganizerId());
                navigateToOrganizerMainPage();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerFacilityActivity.this, "Failed to save facility: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void navigateToOrganizerMainPage() {
        Intent intent = new Intent(OrganizerFacilityActivity.this, OrganizerMainPage.class);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("entrant_data", entrant);
        startActivity(intent);
        finish();
    }
}