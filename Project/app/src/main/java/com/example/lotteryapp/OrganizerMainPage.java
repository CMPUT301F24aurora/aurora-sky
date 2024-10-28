package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerMainPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_main_page);

        // Find the Create Event button by its ID
        Button createEventButton = findViewById(R.id.create_event_button);

        // Set an OnClickListener to navigate to the CreateEventActivity
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });
    }
}
