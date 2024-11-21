package com.example.lotteryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;

import java.util.Objects;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editEmail, editPhone;
    private Button confirmChanges;
    private Button removeProfilePicture;
    private ImageView currentProfilePicture;
    private ImageButton addProfilePictureButton;
    private Uri selectedImageUri = null;
    private String currentUser;
    private boolean profilePictureChanged = false;
    private boolean isInitialSignup = true;  // Set to true during signup, false during edits
    private Entrant entrant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        FirebaseApp.initializeApp(this);

        // Initialize views
        Intent intent = getIntent();
        entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        currentUser = intent.getStringExtra("userType");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        currentProfilePicture = findViewById(R.id.profile_photo);
        addProfilePictureButton = findViewById(R.id.add_profile_picture_button);
        removeProfilePicture = findViewById(R.id.remove_profile_picture);
        confirmChanges = findViewById(R.id.confirm_changes);

        if (entrant != null) {
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());
            isInitialSignup = false;

            if (entrant.getImage_url() != null && !entrant.getImage_url().isEmpty()) {
                // Load image from URL
                Glide.with(this)
                        .load(entrant.getImage_url())
                        .placeholder(R.drawable.ic_profile_photo)
                        .error(R.drawable.ic_profile_photo)
                        .circleCrop()
                        .into(currentProfilePicture);
            } else {
                currentProfilePicture.setImageResource(R.drawable.ic_profile_photo);
            }

            if (isUploadedImage(entrant.getImage_url())) {
                removeProfilePicture.setVisibility(Button.VISIBLE);
            } else {
                removeProfilePicture.setVisibility(Button.GONE);
            }
        } else {
            removeProfilePicture.setVisibility(Button.GONE);
        }

        // Set up listeners
        addProfilePictureButton.setOnClickListener(v -> openImagePicker());
        removeProfilePicture.setOnClickListener(v->removeImage());
        confirmChanges.setOnClickListener(v -> saveEntrantDetails());
    }

    // Helper method to check if the image is uploaded or generated
    private boolean isUploadedImage(String imageUrl) {
        return !imageUrl.contains("generated");
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void removeImage() {
        // Set the profile picture to the default drawable resource
        currentProfilePicture.setImageResource(R.drawable.ic_profile_photo);
        removeProfilePicture.setVisibility(View.GONE);

        // Reset selectedImageUri to null since there's no custom image
        selectedImageUri = null;
        profilePictureChanged = true;
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

        Organizer organizer = new Organizer(deviceId, name, email, phone);

        // Save the organizer first
        DatabaseManager.saveOrganizer(organizer, new SaveOrganizerCallback() {
            @Override
            public void onSuccess() {
                if (isInitialSignup) {
                    // Case 1: Initial signup, upload the image first
                    uploadProfileImage(deviceId, name, email, phone, organizer);
                } else {
                    String image_url = entrant.getImage_url();
                    Log.d("","image url" + image_url);
                    // Case 2: Not initial signup, check if profile picture changed
                    if (profilePictureChanged) {
                        // Case 3: Profile picture changed, upload the new image
                        uploadProfileImage(deviceId, name, email, phone, organizer);
                    } else {
                        // Case 2: No profile picture change, just save the entrant
                        entrant = new Entrant(deviceId, name, email, phone);
                        entrant.setImage_url(image_url);
                        saveEntrantToDatabase(entrant, organizer);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving organizer", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing organizer document", e);
            }
        });
    }


    private void uploadProfileImage(String deviceId, String name, String email, String phone, Organizer organizer) {
        ProfileImage profileImage = new ProfileImage();
        profileImage.uploadImageToFirebase(deviceId, selectedImageUri, name, new ProfileImage.ProfileImageCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Entrant entrant = new Entrant(deviceId, name, email, phone);
                entrant.setImage_url(imageUrl);
                saveEntrantToDatabase(entrant, organizer);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error uploading profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEntrantToDatabase(Entrant entrant, Organizer organizer) {
        DatabaseManager.saveEntrant(entrant, new SaveEntrantCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantProfileEditActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                navigateToNextActivity(entrant, organizer);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving entrant", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing entrant document", e);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profilePictureChanged = true;

            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_profile_photo)
                    .error(R.drawable.ic_profile_photo)
                    .circleCrop()
                    .into(currentProfilePicture);
            removeProfilePicture.setVisibility(View.VISIBLE);
        }
    }
}
