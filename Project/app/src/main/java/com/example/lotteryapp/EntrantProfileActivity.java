package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity";
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
