package com.example.lotteryapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class OrganizerRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "OrganizerRegistration";
    private FirebaseFirestore db;
    private String deviceId;

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText rePasswordEditText;
    private ImageView profileImageView;
    private Button createAccountButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri profileImageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_registration);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        nameEditText = findViewById(R.id.editOrganizerName);
        phoneEditText = findViewById(R.id.editTextPhoneNumber);
        emailEditText = findViewById(R.id.editEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        rePasswordEditText = findViewById(R.id.editTextRePassword);
        profileImageView = findViewById(R.id.profile_image);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        checkIfUserExists(); // Check if user already exists before showing registration form
    }

    private void checkIfUserExists() {
        Log.d(TAG, "Checking if device exists with ID: " + deviceId);
        db.collection("entrants").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Device ID found in entrants collection, proceed to OrganizerMainPage
                        Log.d(TAG, "Device ID found in entrants collection.");
                        navigateToOrganizerMainPage();  // Go to OrganizerMainPage immediately
                    } else {
                        // If not found in entrants, check organizers
                        Log.d(TAG, "Device ID not found in entrants, checking organizers.");
                        checkInOrganizers();  // Proceed to check in organizers collection
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error checking entrants collection", e));
    }

    private void checkInOrganizers() {
        Log.d(TAG, "Checking in organizers collection for device ID: " + deviceId);
        db.collection("organizers").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Device ID found in organizers collection, proceed to OrganizerMainPage
                        Log.d(TAG, "Device ID found in organizers collection.");

                        // Create Entrant object to add to entrants collection
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");

                        Entrant entrant = new Entrant(deviceId, name, email, phone);

                        // Save Entrant to Firestore
                        entrant.saveToFirestore(new SaveEntrantCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Organizer successfully added as an entrant.");

                                // Navigate to OrganizerMainPage
                                navigateToOrganizerMainPage();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Error adding organizer as entrant", e);
                                Toast.makeText(getApplicationContext(), "Error saving organizer as entrant", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        // Device ID not found in either, show registration form
                        Log.d(TAG, "Device ID not found in either entrants or organizers, showing registration form.");
                        initializeRegistration();
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error checking organizers collection", e));
    }


    private void initializeRegistration() {
        // Initialize the registration process if the user is not already registered
        profileImageView.setOnClickListener(v -> openFileChooser());
        createAccountButton.setOnClickListener(v -> registerOrganizer());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
//            profileImageUri = data.getData();
//            profileImageView.setImageURI(profileImageUri);
//        }
//    }

    private void registerOrganizer() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String rePassword = rePasswordEditText.getText().toString();

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> organizerData = new HashMap<>();
        organizerData.put("name", name);
        organizerData.put("phone", phone);
        organizerData.put("email", email);

        db.collection("organizers")
                .add(organizerData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(OrganizerRegistrationActivity.this, "Organizer Registered", Toast.LENGTH_SHORT).show();

                    // If the organizer has a profile image, upload it
                    if (profileImageUri != null) {
                        uploadProfileImage(documentReference.getId());
                    }

                    // Add the organizer as an entrant
                    Entrant entrant = new Entrant(documentReference.getId(), name, email, phone);
                    entrant.saveToFirestore(new SaveEntrantCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("OrganizerRegistration", "Organizer also saved as an entrant.");
                            navigateToOrganizerMainPage();  // Proceed to the Organizer Main Page
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("OrganizerRegistration", "Error saving organizer as entrant", e);
                            Toast.makeText(OrganizerRegistrationActivity.this, "Error saving organizer as entrant", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> Log.w("OrganizerRegistration", "Error adding document", e));
    }

    // Method to navigate to OrganizerMainPage
    private void navigateToOrganizerMainPage() {
        Intent intent = new Intent(OrganizerRegistrationActivity.this, OrganizerMainPage.class);
        startActivity(intent);
        finish();
    }

    private void uploadProfileImage(String organizerId) {
        StorageReference fileRef = storageRef.child(organizerId + ".jpg");
        fileRef.putFile(profileImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("organizers").document(organizerId)
                            .update("profileImageUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> Log.d("OrganizerRegistration", "Profile image URL saved"))
                            .addOnFailureListener(e -> Log.w("OrganizerRegistration", "Error saving image URL", e));
                }))
                .addOnFailureListener(e -> Log.w("OrganizerRegistration", "Error uploading image", e));
    }
}
