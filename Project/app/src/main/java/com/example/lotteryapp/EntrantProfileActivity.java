package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotteryapp.Entrant;
import com.example.lotteryapp.EntrantProfileEditActivity;
import com.example.lotteryapp.GetEntrantCallback;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity";
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;
    private Entrant entrant; // Use this for the class-level variable
    private Button editFacilityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        // Initialize views
        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);
        editFacilityButton = findViewById(R.id.edit_account_button);

        // Try to get Entrant data from Intent
        Intent intent = getIntent();
        entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        if (entrant != null) {
            // If entrant is passed via Intent, update UI directly
            Log.d(TAG, "Entrant object is not null");
            updateUI(entrant);
        } else {
            // If not, fetch Entrant from Firestore
            Entrant.getEntrant(this, new GetEntrantCallback() {
                @Override
                public void onEntrantFound(Entrant fetchedEntrant) {
                    // If entrant is fetched, update UI
                    Log.d(TAG, "Entrant fetched from Firestore.");
                    entrant = fetchedEntrant;
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

        // Edit button to open EntrantProfileEditActivity
        editFacilityButton.setOnClickListener(v -> {
            // Start the EntrantProfileEditActivity with current entrant data
            Intent intentEdit = new Intent(EntrantProfileActivity.this, EntrantProfileEditActivity.class);
            intentEdit.putExtra("entrant_data", entrant);
            startActivity(intentEdit);
        });
    }

    private void updateUI(Entrant entrant) {
        // Update the UI with the fetched or passed entrant details
        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhone());

        Log.d(TAG, "Entrant Name: " + entrant.getName());
        Log.d(TAG, "Entrant Email: " + entrant.getEmail());
        Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
    }
}
