package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerRegistration extends AppCompatActivity {
    private EditText organizerName,organizerPhoneNumber, organizerEmail, organizerPassword, organizerRetypePassword;
    private Button confirmChanges;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_sign_up);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize EditText and Buttons
        organizerName = findViewById(R.id.name);
        organizerEmail = findViewById(R.id.email);
        organizerPhoneNumber = findViewById(R.id.phone_number);
        organizerPassword = findViewById(R.id.password);
        organizerRetypePassword = findViewById(R.id.retype_password);
        confirmChanges = findViewById(R.id.confirm_changes);

        // OnClickListener for Confirm Changes Button
        confirmChanges.setOnClickListener(v -> saveOrganizerDetails());
    }

    private void saveOrganizerDetails() {
        String name = organizerName.getText().toString().trim();
        String email = organizerEmail.getText().toString().trim();
        String phone = organizerPhoneNumber.getText().toString().trim();
        String password = organizerPassword.getText().toString().trim();
        String retypePassword = organizerRetypePassword.getText().toString().trim();
        if (!password.equals(retypePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Organizer organizer = new Organizer(name, email, phone, password);
        // Save organizer to Firestore
        saveOrganizerToFirestore(organizer);
    }

    private void saveOrganizerToFirestore(Organizer organizer) {

        // Save the Organizer object to Firestore
        db.collection("organizers").add(organizer)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d("OrganizerProfileEdit", "DocumentSnapshot successfully written with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show();
                    Log.w("OrganizerProfileEdit", "Error writing document", e);
                });

    }
}
