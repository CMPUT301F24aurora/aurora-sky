package com.example.lotteryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerFacilityActivity extends AppCompatActivity {

    private EditText nameField, timeField, locationField, emailField;
    private Button saveButton, removeButton;
    private Facility facility;
    private Organizer organizer;
    private boolean isEditing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_facility);

        nameField = findViewById(R.id.name_field);
        timeField = findViewById(R.id.time_field);
        locationField = findViewById(R.id.location_field);
        emailField = findViewById(R.id.email_field);
        saveButton = findViewById(R.id.save_button);
        removeButton = findViewById(R.id.remove_button);

        // Retrieve facility data to determine if in editing mode
        facility = (Facility) getIntent().getSerializableExtra("facility_data");
        isEditing = (facility != null);

        if (isEditing) {
            // Populate fields with facility data for editing
            nameField.setText(facility.getName());
            timeField.setText(facility.getTime());
            locationField.setText(facility.getLocation());
            emailField.setText(facility.getEmail());
            removeButton.setVisibility(View.VISIBLE);
        } else {
            // New facility setup
            facility = new Facility();
            removeButton.setVisibility(View.GONE);
        }

        setupSaveButton();
        setupRemoveButton();
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            facility.setName(nameField.getText().toString());
            facility.setTime(timeField.getText().toString());
            facility.setLocation(locationField.getText().toString());
            facility.setEmail(emailField.getText().toString());

            Facility.FacilityCallback callback = new Facility.FacilityCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(OrganizerFacilityActivity.this, isEditing ? "Facility updated" : "Facility created", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerFacilityActivity.this, "Operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };

            if (isEditing) {
                facility.updateInFirestore(callback);
            } else {
                facility.saveToFirestore(callback);
            }
        });
    }

    private void setupRemoveButton() {
        removeButton.setOnClickListener(v -> {
            facility.deleteFromFirestore(new Facility.FacilityCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(OrganizerFacilityActivity.this, "Facility removed", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerFacilityActivity.this, "Operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

