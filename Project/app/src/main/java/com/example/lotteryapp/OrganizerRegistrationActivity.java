package com.example.lotteryapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri profileImageUri;

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText rePasswordEditText;
    private ImageView profileImageView;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_registration);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        nameEditText = findViewById(R.id.editOrganizerName);
        phoneEditText = findViewById(R.id.editTextPhoneNumber);
        emailEditText = findViewById(R.id.editNumberOfMembers);
        passwordEditText = findViewById(R.id.editTextPassword);
        rePasswordEditText = findViewById(R.id.editTextRePassword);
        profileImageView = findViewById(R.id.profile_image);
        createAccountButton = findViewById(R.id.buttonCreateAccount);

        profileImageView.setOnClickListener(v -> openFileChooser());
        createAccountButton.setOnClickListener(v -> registerOrganizer());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }

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
                    if (profileImageUri != null) {
                        uploadProfileImage(documentReference.getId());
                    }
                })
                .addOnFailureListener(e -> Log.w("OrganizerRegistration", "Error adding document", e));
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
