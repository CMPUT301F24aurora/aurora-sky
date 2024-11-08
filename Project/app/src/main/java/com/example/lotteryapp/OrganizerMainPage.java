package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizerMainPage extends AppCompatActivity {

    private Button createEventButton;
    private Button createFacilityButton;
    private Button manageFacilitiesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_main_page);

        createEventButton = findViewById(R.id.create_event_button);
        createFacilityButton = findViewById(R.id.create_facility_button);
        manageFacilitiesButton = findViewById(R.id.manage_facilities_button);

        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });

        createFacilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityActivity.class);
            startActivity(intent);
        });

        manageFacilitiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityListActivity.class);
            startActivity(intent);
        });
    }
}


