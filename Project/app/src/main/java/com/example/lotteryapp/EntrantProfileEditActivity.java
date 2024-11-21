package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;

import java.util.Objects;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";

    private EditText editName, editEmail, editPhone;
    private Button confirmChanges;
    private ImageView currentProfilePicture;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        FirebaseApp.initializeApp(this);

        // Initialize EditText and Buttons
        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        currentUser = intent.getStringExtra("userType");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        currentProfilePicture = findViewById(R.id.profile_photo);  // If required for displaying an existing image
        confirmChanges = findViewById(R.id.confirm_changes);

        if (entrant != null) {
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());

            if (entrant.getImage_url() != null && !entrant.getImage_url().isEmpty()) {
                // Load the image from the URL
                Glide.with(this)
                        .load(entrant.getImage_url())
                        .placeholder(R.drawable.ic_profile_photo) // Optional: Placeholder image
                        .error(R.drawable.ic_profile_photo) // Optional: Fallback image on error
                        .circleCrop() // Circular cropping
                        .into(currentProfilePicture);
            } else {
                // Fallback to default image
                currentProfilePicture.setImageResource(R.drawable.ic_profile_photo);
            }
        }

        // Set up listeners
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
        DatabaseManager.saveOrganizer(organizer, new SaveOrganizerCallback() {
            @Override
            public void onSuccess() {
                createOrUpdateEntrant(deviceId, name, email, phone, organizer);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving organizer", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing organizer document", e);
            }
        });
    }

    private void createOrUpdateEntrant(String deviceId, String name, String email, String phone, Organizer organizer) {
        Entrant entrant = new Entrant(deviceId, name, email, phone);
        ProfileImage profileImage = new ProfileImage();

        profileImage.ensureProfileImage(deviceId, name, new ProfileImage.ProfileImageCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                entrant.setImage_url(imageUrl); // Save image URL in the entrant object
                DatabaseManager.saveEntrant(entrant, new SaveEntrantCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EntrantProfileEditActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        navigateToNextActivity(entrant, organizer);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantProfileEditActivity.this, "Error saving profile", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error writing document", e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error generating profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void navigateToNextActivity(Entrant entrant, Organizer organizer) {
        Intent intent;
        if (Objects.equals(currentUser, "organizer")) {
            intent = new Intent(EntrantProfileEditActivity.this, OrganizerMainPage.class);
        } else {
            intent = new Intent(EntrantProfileEditActivity.this, EntrantsEventsActivity.class);
        }
        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);
        startActivity(intent);
    }
}