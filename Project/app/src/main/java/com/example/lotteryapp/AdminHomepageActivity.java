package com.example.lotteryapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The AdminHomepageActivity class represents the homepage for admin users.
 * It provides navigation to different sections such as viewing and editing events, facilities, and profiles.
 *
 * @see AppCompatActivity
 * @see AdminViewEditEventsActivity
 * @see AdminViewEditFacilitiesActivity
 * @see AdminViewEditProfilesActivity
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminHomepageActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * This method sets up the layout and initializes the buttons for navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view);

        Button viewEvents = findViewById(R.id.admin_v_ev);
        Button viewFacilities = findViewById(R.id.admin_v_fac);
        Button viewProfiles = findViewById(R.id.admin_v_pro);

        viewEvents.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the view events button is clicked.
             * This method starts the AdminViewEditEventsActivity.
             *
             * @param v the view that was clicked
             * @see AdminViewEditEventsActivity
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditEventsActivity.class);
                startActivity(intent);
            }
        });

        viewFacilities.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the view facilities button is clicked.
             * This method starts the AdminViewEditFacilitiesActivity.
             *
             * @param v the view that was clicked
             * @see AdminViewEditFacilitiesActivity
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditFacilitiesActivity.class);
                startActivity(intent);
            }
        });

        viewProfiles.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the view profiles button is clicked.
             * This method starts the AdminViewEditProfilesActivity.
             *
             * @param v the view that was clicked
             * @see AdminViewEditProfilesActivity
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditProfilesActivity.class);
                startActivity(intent);
            }
        });
    }
}

