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

/**
 * Activity for managing the organizer's facility.
 * <p>
 * This activity allows the organizer to either create a new facility or edit an existing one. It includes fields for the facility name, location, email, and operation hours,
 * and it provides functionality to save or remove the facility details.
 * </p>
 *
 * @see AppCompatActivity
 * @see Facility
 * @see FacilityDatabase
 * @see Organizer
 */
public class OrganizerFacilityActivity extends AppCompatActivity {

    private EditText nameField, locationField, emailField;
    private Button startTimeButton, endTimeButton, saveButton, removeButton;
    private Facility facility;
    private boolean isEditing;
    private Organizer organizer;
    private Entrant entrant;
    private FacilityDatabase facilityDatabase;

    /**
     * Called when the activity is first created.
     * <p>
     * This method initializes the views, sets up the facility data, and configures buttons for selecting start and end times.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down,
     *                           this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}. Otherwise, it is null.
     * @return void
     * @see #initializeViews()
     * @see #setupFacilityData()
     * @see #setupTimeButtons()
     */
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

    /**
     * Initializes the views for the facility management activity.
     * <p>
     * This method sets up the EditText fields for the name, location, and email of the facility,
     * and the buttons for selecting start and end times, saving the facility data, and removing the facility.
     * </p>
     *
     * @return void
     * @see #setupFacilityData()
     * @see #setupTimeButtons()
     * @see #setupSaveButton()
     */
    private void initializeViews() {
        nameField = findViewById(R.id.name_field);
        startTimeButton = findViewById(R.id.start_time_button);
        endTimeButton = findViewById(R.id.end_time_button);
        locationField = findViewById(R.id.location_field);
        emailField = findViewById(R.id.email_field);
        saveButton = findViewById(R.id.save_button);
        removeButton = findViewById(R.id.remove_button);
    }

    /**
     * Sets up the facility data based on the current organizer's facility information.
     * <p>
     * If the organizer has an existing facility, the facility data is loaded; otherwise, a new facility object is created.
     * </p>
     *
     * @return void
     * @see #loadFacilityData(String)
     */
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

    /**
     * Loads the existing facility data from the database.
     * <p>
     * The facility data is retrieved by its unique ID, and if successful, the data is populated into the UI.
     * If loading fails, the activity is finished with an error message.
     * </p>
     *
     * @param facilityId The ID of the facility to load.
     * @return void
     * @see FacilityDatabase
     */
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

    /**
     * Populates the UI with the existing facility data.
     * <p>
     * This method sets the text of the input fields and buttons to reflect the current facility data.
     * </p>
     *
     * @return void
     * @see Facility
     */
    private void populateFacilityData() {
        nameField.setText(facility.getName());
        startTimeButton.setText("Start Time: " + facility.getStartTime());
        endTimeButton.setText("End Time: " + facility.getEndTime());
        locationField.setText(facility.getLocation());
        emailField.setText(facility.getEmail());
    }

    /**
     * Sets up the functionality for the start and end time buttons.
     * <p>
     * When either the start time or end time button is clicked, a time picker dialog is shown to allow the user to select the time.
     * </p>
     *
     * @return void
     * @see #showTimePicker(boolean)
     */
    private void setupTimeButtons() {
        startTimeButton.setOnClickListener(v -> showTimePicker(true));
        endTimeButton.setOnClickListener(v -> showTimePicker(false));
    }

    /**
     * Displays a time picker dialog to allow the user to select a time.
     * <p>
     * The method is used for both the start and end time fields. The selected time is then set to the corresponding field.
     * </p>
     *
     * @param isStartTime A boolean indicating whether the selected time is for the start or end time.
     * @return void
     * @see TimePickerDialog
     */
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

    /**
     * Sets up the functionality for the save button.
     * <p>
     * When the save button is clicked, the input fields are validated, the facility data is updated, and it is saved to the database.
     * </p>
     *
     * @return void
     * @see #validateInputs()
     * @see #updateFacilityData()
     * @see #saveFacilityToFirestore()
     */
    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateFacilityData();
                saveFacilityToFirestore();
            }
        });
    }

    /**
     * Validates the input fields to ensure that the required data is provided and in the correct format.
     * <p>
     * The method checks for valid input in the name, location, email, start time, and end time fields.
     * </p>
     *
     * @return boolean Returns true if all inputs are valid, false otherwise.
     * @see android.util.Patterns
     */
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