package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity";
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;
    private Entrant entrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);

        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        if (entrant != null) {
            Log.d(TAG, "Entrant object is not null");
            updateUI(entrant);
        } else {
            Entrant.getEntrant(this, new GetEntrantCallback() {
                @Override
                public void onEntrantFound(Entrant fetchedEntrant) {
                    Log.d(TAG, "Entrant fetched from Firestore.");
                    updateUI(fetchedEntrant);
                }

                @Override
                public void onEntrantNotFound(Exception e) {
                    Log.w(TAG, "Entrant not found", e);
                    Toast.makeText(EntrantProfileActivity.this, "Entrant not found", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error fetching entrant", e);
                    Toast.makeText(EntrantProfileActivity.this, "Error fetching entrant", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button editFacilityButton = findViewById(R.id.edit_account_button);
        editFacilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the EntrantProfileEditActivity
                Intent intent = new Intent(EntrantProfileActivity.this, EntrantProfileEditActivity.class);

                // Assuming you have the Entrant object available as 'entrant'
                intent.putExtra("entrant_data", entrant);

                startActivity(intent);
            }
        });

        Button editAccountButton = findViewById(R.id.edit_account_button);
        editAccountButton.setOnClickListener(v -> {
            // Start the Organizer Main Page when the organizer button is clicked
            Intent editIntent = new Intent(EntrantProfileActivity.this, EntrantProfileEditActivity.class);
            startActivity(editIntent);
        });
    }

    private void updateUI(Entrant entrant) {
        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhone());

        Log.d(TAG, "Entrant Name: " + entrant.getName());
        Log.d(TAG, "Entrant Email: " + entrant.getEmail());
        Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
    }
}
