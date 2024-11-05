package com.example.lotteryapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";

    private EditText editName, editEmail, editPhone;
    private Button removeProfilePicture, confirmChanges;
    private ImageView currentProfilePicture;
    private ImageButton addProfilePictureButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri profileImageUri;

    // Declare the ActivityResultLauncher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        // Initialize EditText and Buttons
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        removeProfilePicture = findViewById(R.id.remove_profile_picture);
        addProfilePictureButton = findViewById(R.id.add_profile_picture_button);
        currentProfilePicture = findViewById(R.id.profile_photo);
        confirmChanges = findViewById(R.id.confirm_changes);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            profileImageUri = data.getData();
                            currentProfilePicture.setImageURI(profileImageUri);
                        }
                    }
                });


        // OnClickListener for Confirm Changes Button
        confirmChanges.setOnClickListener(v -> saveEntrantDetails());
        addProfilePictureButton.setOnClickListener(v -> openImageChooser());

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Entrant entrant, Uri imageUri) {
        StorageReference fileRef = storageRef.child(entrant.getId() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveEntrantToFirestore(entrant, imageUrl);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to get download URL", e)))
                .addOnFailureListener(e -> Log.e(TAG, "Profile image upload failed", e));
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

        // Create Entrant object
        Entrant entrant;
        if (phone.isEmpty()) {
            entrant = new Entrant(deviceId, name, email);
        } else {
            entrant = new Entrant(deviceId, name, email, phone);
        }

        // Check if a profile picture has been selected
        if (profileImageUri != null) {
            uploadProfileImage(entrant, profileImageUri);
        } else {
            saveEntrantToFirestore(entrant, null);
        }
    }
    private void saveEntrantToFirestore(Entrant entrant, String imageUrl) {
        if (imageUrl != null) {
            entrant.setProfileImageUrl(imageUrl);
        }

        entrant.saveToFirestore(new SaveEntrantCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EntrantProfileEditActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "DocumentSnapshot successfully written!");

                // Navigate to EntrantEventsActivity after successful save
                Intent intent = new Intent(EntrantProfileEditActivity.this, EntrantsEventsActivity.class);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ImageView profilePhoto = findViewById(R.id.profile_photo);
            profilePhoto.setImageURI(imageUri);
        }
    }
}
