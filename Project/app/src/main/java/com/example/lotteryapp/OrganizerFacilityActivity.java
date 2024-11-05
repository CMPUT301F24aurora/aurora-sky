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

        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        facility = (Facility) getIntent().getSerializableExtra("facility_data");

        isEditing = (facility != null);


        if (isEditing) {
            nameField.setText(facility.getName());
            timeField.setText(facility.getTime());
            locationField.setText(facility.getLocation());
            emailField.setText(facility.getEmail());
            removeButton.setVisibility(View.VISIBLE);
        } else {
            facility = new Facility();
            removeButton.setVisibility(View.GONE);
        }


        if (organizer != null && organizer.hasOrganizerPermissions()) {
            setupSaveButton();
            setupRemoveButton();
        } else {
            disableEditing();
        }
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {

            facility.setName(nameField.getText().toString());
            facility.setTime(timeField.getText().toString());
            facility.setLocation(locationField.getText().toString());
            facility.setEmail(emailField.getText().toString());

            Facility.FacilityCallback callback = new Facility.FacilityCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(OrganizerFacilityActivity.this, message, Toast.LENGTH_SHORT).show();
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
                public void onSuccess(String message) {
                    Toast.makeText(OrganizerFacilityActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerFacilityActivity.this, "Operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void disableEditing() {

        nameField.setEnabled(false);
        timeField.setEnabled(false);
        locationField.setEnabled(false);
        emailField.setEnabled(false);
        saveButton.setVisibility(View.GONE);
        removeButton.setVisibility(View.GONE);
    }
}
