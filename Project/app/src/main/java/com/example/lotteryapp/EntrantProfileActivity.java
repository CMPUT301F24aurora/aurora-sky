package com.example.lotteryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity";
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;
    private Entrant entrant;
    private ImageView profileImageView;
    private Button uploadImageButton;
    private Button removeImageButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);
        profileImageView = findViewById(R.id.profile_picture);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        if (entrant != null) {
            Log.d(TAG, "Entrant object is not null");
            updateUI(entrant);
        } else {
            Entrant.getEntrant(this, new GetEntrantCallback() {
                @Override
                public void onEntrantFound(Entrant fetchedEntrant) {
                    Log.d(TAG, "Entrant fetched from Firestore.");
                    updateUI(fetchedEntrant);
                }

                @Override
                public void onEntrantNotFound(Exception e) {
                    Log.w(TAG, "Entrant not found", e);
                    Toast.makeText(EntrantProfileActivity.this, "Entrant not found", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error fetching entrant", e);
                    Toast.makeText(EntrantProfileActivity.this, "Error fetching entrant", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button editFacilityButton = findViewById(R.id.edit_account_button);
        editFacilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the EntrantProfileEditActivity
                Intent intentEdit = new Intent(EntrantProfileActivity.this, EntrantProfileEditActivity.class);

                // Assuming you have the Entrant object available as 'entrant'
                intentEdit.putExtra("entrant_data", entrant);

                startActivity(intentEdit);
            }
        });
    }

    private void updateUI(Entrant entrant) {
        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhone());
        String profileImageUrl = entrant.getProfileImageUrl();

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Picasso.get().load(profileImageUrl).into(profileImageView);
        } else {
            Bitmap avatar = AvatarUtils.generateAvatarWithInitial(entrant.getName());
            profileImageView.setImageBitmap(avatar);
            uploadAvatarToFirebase(avatar);
        }
    }

    private void uploadAvatarToFirebase(Bitmap avatar) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avatar.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        String avatarFileName = "avatars/" + UUID.randomUUID().toString() + ".png";
        StorageReference avatarRef = storageReference.child(avatarFileName);

        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveAvatarUrlToFirestore(uri.toString());
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to upload avatar", e);
            Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveAvatarUrlToFirestore(String avatarUrl) {
        if (entrant != null) {
            entrant.setProfileImageUrl(avatarUrl);
            firestore.collection("entrants")
                    .document(entrant.getId()) // assuming you have an entrant ID
                    .update("profileImageUrl", avatarUrl)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Avatar URL saved to Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save avatar URL to Firestore", e));
        }
    }

}
