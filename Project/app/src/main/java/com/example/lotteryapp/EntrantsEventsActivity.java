package com.example.lotteryapp;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lotteryapp.EntrantProfileActivity;

public class EntrantsEventsActivity extends AppCompatActivity {

    private ImageButton profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);

        // Initialize the profile icon button
        profileIcon = findViewById(R.id.profile_icon);

        // Set an OnClickListener to navigate to the profile page
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantsEventsActivity.this, EntrantProfileActivity.class);
            startActivity(intent);
        });
    }
}
