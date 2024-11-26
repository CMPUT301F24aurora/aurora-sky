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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminViewProfilesContent extends AppCompatActivity {

    private TextView entrantNameTextView;
    private TextView entrantPhoneTextView;
    private TextView entrantEmailTextView;
    private Button removeEntrantButton;
    private Button removePictureButton;
    private String entrantId; // This should be passed to the activity
    private FirebaseFirestore db;
    private ImageView profilePicture;
    private String entrantName;
    private String entrantPhone;
    private String entrantEmail;
    private String entrantPhoto;

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
        removePictureButton = findViewById(R.id.admin_ent_img_remove);
        profilePicture = findViewById(R.id.profile_picture);

        entrantName = intent.getStringExtra("entrantName");
        entrantPhone = intent.getStringExtra("entrantPhone");
        entrantEmail = intent.getStringExtra("entrantEmail");
        entrantPhoto = intent.getStringExtra("entrantPhoto");
        entrantNameTextView.setText(entrantName);
        entrantEmailTextView.setText(entrantEmail);
        entrantPhoneTextView.setText(entrantPhone);
        entrantPhoneTextView.setText(entrantPhone);

        // Set up the remove button's click listener
        removeEntrantButton.setOnClickListener(view -> {
            // Delete the entrant from Firestore
            deleteEntrant();
        });

        if (entrantPhoto != null && !entrantPhoto.isEmpty()) {
            Glide.with(this)
                    .load(entrantPhoto)
                    .placeholder(R.drawable.ic_profile_photo) // Fallback placeholder image
                    .error(R.drawable.ic_profile_photo) // Fallback error image
                    .circleCrop() // Make image circular
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.ic_profile_photo); // Default placeholder
        }

        removePictureButton.setOnClickListener(view -> {
            // Delete the image url field from entrant
            if (entrantPhoto != null) {
                removeImage(entrantId, entrantName);
            }
            else{
                Toast.makeText(AdminViewProfilesContent.this, "No Profile Image to delete!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeImage(String entrantId, String entrantName) {
        // Delete the image from Firebase Storage first
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(entrantPhoto);

        imageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Successfully deleted the image from Firebase Storage
                    Toast.makeText(AdminViewProfilesContent.this, "Image successfully deleted from Firebase Storage", Toast.LENGTH_SHORT).show();

                    // Set image_url to null in Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("image_url", null);

                    db.collection("entrants").document(entrantId)
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener(aVoid1 -> {
                                // Successfully updated Firestore
                                Log.d("AdminRemoveProfileImage","Profile Image successfully deleted!");

                                // Generate a new arbitrary profile image
                                ProfileImage profileImage = new ProfileImage();
                                profileImage.uploadImageToFirebase(entrantId, null, entrantName, new ProfileImage.ProfileImageCallback() {
                                    @Override
                                    public void onSuccess(String imageUrl) {
                                        // Update Firestore with the new image URL
                                        Map<String, Object> newUpdates = new HashMap<>();
                                        newUpdates.put("image_url", imageUrl);

                                        db.collection("entrants").document(entrantId)
                                                .set(newUpdates, SetOptions.merge())
                                                .addOnSuccessListener(aVoid2 -> {
                                                    // Successfully updated Firestore with the new image URL
                                                    Log.d("AdminRemoveProfileImage", "Arbitrary profile image URL updated in Firestore");

                                                    // Clear the cached image and update UI
                                                    Glide.with(AdminViewProfilesContent.this).load(imageUrl).into(profilePicture);

                                                    // Reload the activity with the new profile image
                                                    Intent intent = new Intent(AdminViewProfilesContent.this, AdminViewProfilesContent.class);
                                                    intent.putExtra("entrantName", entrantName);
                                                    intent.putExtra("entrantId", entrantId);
                                                    intent.putExtra("entrantEmail", entrantEmail);
                                                    intent.putExtra("entrantPhone", entrantPhone);
                                                    intent.putExtra("entrantPhoto", imageUrl);
                                                    finish();
                                                    startActivity(intent);
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle Firestore update failure
                                                    Log.e("AdminRemoveProfileImage", "Error updating Firestore with arbitrary image URL " + e.getMessage());
                                                });
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Handle image upload failure
                                        Log.e("AdminRemoveProfileImage", "Error uploading arbitrary profile image " + e.getMessage());
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                // Handle Firestore update failure
                                Log.e("AdminRemoveProfileImage", "Error updating Firestore " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle Firebase Storage deletion failure
                    Log.e("AdminRemoveProfileImage", "Error deleting image from Firebase Storage " + e.getMessage());
                });
    }


    private void deleteEntrant() {
        if (entrantId == null) return;

        db.collection("entrants").document(entrantId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminViewProfilesContent.this, "Entrant removed successfully", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminViewProfilesContent.this, "Failed to remove entrant: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            updateWaitingList(document.getId(), entrantId);
                        }
                    } else {
                        Log.e("AdminViewProfilesContent", "Error fetching event documents." + task.getException());
                    }
                });

        // Navigate back to AdminViewEditProfiles activity
        Intent intent = new Intent(AdminViewProfilesContent.this, AdminViewEditProfilesActivity.class);
        startActivity(intent);
        finish();
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
                        Log.e("AdminViewProfilesContentActivity", "Event does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminViewProfilesContentActivity", "Error getting event document." + e);
                });
    }
}
