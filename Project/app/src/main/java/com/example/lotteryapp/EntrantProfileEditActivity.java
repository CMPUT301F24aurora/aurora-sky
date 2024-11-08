package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";

    private EditText editName, editEmail, editPhone;
    private Button confirmChanges;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        confirmChanges = findViewById(R.id.confirm_changes);

        if (entrant != null) {
            Log.d(TAG, "Entrant Name: " + entrant.getName());
            Log.d(TAG, "Entrant Email: " + entrant.getEmail());
            Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());
        } else {
            Log.d(TAG, "Entrant is null");
        }

        confirmChanges.setOnClickListener(v -> saveEntrantDetails());
    }

    private void saveEntrantDetails() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            Toast.makeText(this, "Invalid phone number. Please enter a 10-digit number.", Toast.LENGTH_SHORT).show();
            return;
        }

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create or update Organizer object
        Organizer organizer = new Organizer(deviceId, name, email, phone);
        organizer.saveToFirestore(new SaveOrganizerCallback() {
            @Override
            public void onSuccess() {
                // Organizer saved successfully, now create/update Entrant
                createOrUpdateEntrant(deviceId, name, email, phone);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving organizer", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing organizer document", e);
            }
        });
    }

    private void createOrUpdateEntrant(String deviceId, String name, String email, String phone) {
        Entrant entrant;
        if (phone.isEmpty()) {
            entrant = new Entrant(deviceId, name, email, "");
        } else {
            entrant = new Entrant(deviceId, name, email, phone, "");
        }

        saveEntrantToFirestore(entrant);
    }

    private void saveEntrantToFirestore(Entrant entrant) {
        entrant.saveToFirestore(new SaveEntrantCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantProfileEditActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "DocumentSnapshot successfully written!");

                // Get the userRole from the Intent
                String userRole = getIntent().getStringExtra("userRole");

                // Navigate to the appropriate activity based on userRole
                Intent intent;
                if ("organizer".equals(userRole)) {
                    intent = new Intent(EntrantProfileEditActivity.this, OrganizerMainPage.class);
                } else { // Default to EntrantEventsActivity if no role or "entrant"
                    intent = new Intent(EntrantProfileEditActivity.this, EntrantsEventsActivity.class);
                }
                intent.putExtra("entrant_data", entrant);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing document", e);
            }
        });
    }
}