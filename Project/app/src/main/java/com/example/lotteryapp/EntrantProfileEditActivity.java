package com.example.lotteryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPhone;
    private Button updateProfilePicture, confirmChanges;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize EditText and Buttons
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        updateProfilePicture = findViewById(R.id.update_profile_picture);
        confirmChanges = findViewById(R.id.confirm_changes);

        // OnClickListener for Confirm Changes Button
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

        // Get the device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create an Entrant object with or without Phone
        if(phone.isEmpty()){
            Entrant entrant = new Entrant(deviceId, name, email);
            // Save Entrant to Firestore
            saveEntrantToFirestore(entrant);
        } else {
            Entrant entrant = new Entrant(deviceId, name, email, phone);
            // Save Entrant to Firestore
            saveEntrantToFirestore(entrant);
        }


    }

    private void saveEntrantToFirestore(Entrant entrant) {

        // Save the Entrant object to Firestore
        db.collection("entrants").document(entrant.getId()) // Use device ID as document ID
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d("EntrantProfileEdit", "DocumentSnapshot successfully written!");
                    // Navigate to EntrantEventsActivity after successful save
                    Intent intent = new Intent(EntrantProfileEditActivity.this, EntrantsEventsActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
                    Log.w("EntrantProfileEdit", "Error writing document", e);
                });
    }

}
