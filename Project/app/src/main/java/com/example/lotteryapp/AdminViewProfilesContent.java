package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminViewProfilesContent extends AppCompatActivity {

    private TextView entrantNameTextView;
    private TextView entrantPhoneTextView;
    private TextView entrantEmailTextView;
    private Button removeEntrantButton;
    private String entrantId; // This should be passed to the activity
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_entrants_content);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the Intent that started this activity and extract the entrant ID
        Intent intent = getIntent();
        entrantId = intent.getStringExtra("entrantId"); // Ensure you pass this when starting the activity

        // Initialize views
        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        removeEntrantButton = findViewById(R.id.admin_ev_ent_remove);

        String entrantName = intent.getStringExtra("entrantName");
        String entrantPhone = intent.getStringExtra("entrantPhone");
        String entrantEmail = intent.getStringExtra("entrantEmail");
        entrantNameTextView.setText(entrantName);
        entrantEmailTextView.setText(entrantEmail);
        entrantPhoneTextView.setText(entrantPhone);

        // Set up the remove button's click listener
        removeEntrantButton.setOnClickListener(view -> {
            // Delete the entrant from Firestore
            deleteEntrant();
        });
    }

    private void deleteEntrant() {
        if (entrantId == null) return;

        db.collection("entrants").document(entrantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminViewProfilesContent.this, "Entrant removed successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to AdminViewEditProfiles activity
                    Intent intent = new Intent(AdminViewProfilesContent.this, AdminViewEditProfilesActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminViewProfilesContent.this, "Failed to remove entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
