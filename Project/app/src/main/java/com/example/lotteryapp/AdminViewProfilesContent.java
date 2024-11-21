package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewProfilesContent extends AppCompatActivity {

    private TextView entrantNameTextView;
    private TextView entrantPhoneTextView;
    private TextView entrantEmailTextView;
    private ImageView entrantImageView;
    private Button removeEntrantButton;
    private Button removeEntrantImageButton;
    private String entrantId; // This should be passed to the activity
    private FirebaseFirestore db;
    private String entrantName;
    private String entrantPhone;
    private String entrantEmail;
    private String entrantImageUrl;

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
        removeEntrantImageButton = findViewById(R.id.admin_ent_img_remove);
        entrantImageView = findViewById(R.id.profile_picture);

        entrantName = intent.getStringExtra("entrantName");
        entrantPhone = intent.getStringExtra("entrantPhone");
        entrantEmail = intent.getStringExtra("entrantEmail");
        entrantImageUrl = intent.getStringExtra("entrantImage");
        entrantNameTextView.setText(entrantName);
        entrantEmailTextView.setText(entrantEmail);
        entrantPhoneTextView.setText(entrantPhone);
        if (entrantImageUrl != null) {
            Glide.with(this) .load(entrantImageUrl) .into(entrantImageView);
        }

        // Set up the remove button's click listener
        removeEntrantButton.setOnClickListener(view -> {
            // Delete the entrant from Firestore
            deleteEntrant();
        });

        removeEntrantImageButton.setOnClickListener(view -> {
            // Delete the image url field from entrant
            if (entrantImageUrl != null) {
                deleteImageUrlField(entrantId);
            }
            else{
                Toast.makeText(AdminViewProfilesContent.this, "No Profile Image to delete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImageUrlField(String entrantId) {
        // Create a map with the field to be deleted
        Map<String, Object> updates = new HashMap<>();
        updates.put("image_url", null);

        // Update the document with the entrantId to set image_url to null
        db.collection("entrants").document(entrantId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Successfully deleted the image_url field
                    Toast.makeText(AdminViewProfilesContent.this, "Profile Image successfully deleted!", Toast.LENGTH_SHORT).show();
                    // Clear the cached image
                    Glide.with(AdminViewProfilesContent.this) .load((String) null) .into(entrantImageView);
                    Intent intent = new Intent(this, AdminViewProfilesContent.class);
                    intent.putExtra("entrantName", entrantName);
                    intent.putExtra("entrantId", entrantId);
                    intent.putExtra("entrantEmail", entrantEmail);
                    intent.putExtra("entrantPhone", entrantPhone);
                    intent.putExtra("entrantImage", entrantImageUrl);
                    finish();
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Log.e("AdminRemoveProfileImage", "Error deleting profile image " + e.getMessage());
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
        //Also remove entrant form the event waiting lists:
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            updateWaitingList(document.getId(), entrantId);
                        }
                    } else {
                        Toast.makeText(AdminViewProfilesContent.this, "Error getting documents." + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWaitingList(String eventId, String entrantId) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");

                        if (waitingList != null) {
                            // Remove all occurrences of entrantId
                            waitingList.removeIf(entrantId::equals);

                            // Update the document with the new waiting list
                            Map<String, Object> updates = Map.of("waitingList", waitingList);
                            db.collection("events").document(eventId).set(updates, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d("AdminViewProfilesContentActivity", "Waiting list updated successfully."))
                                    .addOnFailureListener(e -> Log.w("AdminViewProfilesContentActivity", "Error updating waiting list.", e));
                        }
                    } else {
                        Log.w("AdminViewProfilesContentActivity", "Event does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminViewProfilesContent.this, "Error getting document." + e, Toast.LENGTH_SHORT).show();
                });
    }
}
