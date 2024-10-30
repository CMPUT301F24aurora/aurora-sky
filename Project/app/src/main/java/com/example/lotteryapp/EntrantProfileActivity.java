package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity"; // Tag for logging
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);

        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        // Log to check if entrant is null
        if (entrant != null) {
            Log.d(TAG, "Entrant object is not null");
            entrantNameTextView.setText(entrant.getName());
            entrantEmailTextView.setText(entrant.getEmail());
            entrantPhoneTextView.setText(entrant.getPhone());

            // Log entrant's properties
            Log.d(TAG, "Entrant Name: " + entrant.getName());
            Log.d(TAG, "Entrant Email: " + entrant.getEmail());
            Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
        } else {
            Log.d(TAG, "Entrant object is null");
        }

        Button mapButton = findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EntrantProfileActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
