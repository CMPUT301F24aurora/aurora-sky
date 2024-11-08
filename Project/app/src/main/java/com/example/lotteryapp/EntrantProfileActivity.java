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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.google.firebase.storage.FirebaseStorage;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);
        profileImageView = findViewById(R.id.profile_picture);
        uploadImageButton = findViewById(R.id.upload_image_button);
        removeImageButton = findViewById(R.id.remove_image_button);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

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

        uploadImageButton.setOnClickListener(v -> openFileChooser());
        removeImageButton.setOnClickListener(v -> removeProfileImage());

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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("profile_images/" + UUID.randomUUID().toString());

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                entrant.setProfileImageUrl(imageUrl);

                // Save to Firestore with callback handling
                entrant.saveToFirestore(new SaveEntrantCallback() {
                    @Override
                    public void onSuccess() {
                        Picasso.get().load(imageUrl).into(profileImageView);
                        Toast.makeText(EntrantProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                    }
                });
            })).addOnFailureListener(e -> Toast.makeText(EntrantProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
        }
    }


    private void removeProfileImage() {
        entrant.removeProfileImage();
        entrant.saveToFirestore(new SaveEntrantCallback() {
            @Override
            public void onSuccess() {
                // Revert to avatar with the first letter of the entrant's name
                Bitmap avatar = generateAvatarWithInitial(entrant.getName());
                profileImageView.setImageBitmap(avatar);
                Toast.makeText(EntrantProfileActivity.this, "Profile image removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileActivity.this, "Failed to remove profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Entrant entrant) {
        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhone());
        String profileImageUrl = entrant.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // Load the profile image if the URL is not empty or null
            Picasso.get().load(profileImageUrl).into(profileImageView);
        } else {
            // Generate an avatar with the first letter of the entrant's name
            Bitmap avatar = generateAvatarWithInitial(entrant.getName());
            profileImageView.setImageBitmap(avatar);
        }

        Log.d(TAG, "Entrant Name: " + entrant.getName());
        Log.d(TAG, "Entrant Email: " + entrant.getEmail());
        Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
        Log.d(TAG, "Entrant Profile Picture: " + entrant.getProfileImageUrl());
    }

    private Bitmap generateAvatarWithInitial(String name) {
        String initial = name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        int size = 100; // Define icon size
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Use the getColorFromHash method to generate a consistent background color
        int color = AvatarUtils.getColorFromHash(name);

        Paint paint = new Paint();
        paint.setColor(color); // Set the generated background color
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size, paint);

        paint.setColor(Color.WHITE); // Set text color
        paint.setTextSize(40); // Set text size
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw the initial in the center of the bitmap
        canvas.drawText(initial, size / 2, size / 2 - ((paint.descent() + paint.ascent()) / 2), paint);

        return bitmap;
    }


    public static class AvatarUtils {
        public static int getColorFromHash(String name) {
            String hash = hash(name);
            if (hash == null || hash.isEmpty()) return Color.GRAY;
            return Color.parseColor("#" + hash.substring(0, 6));
        }

        private static String hash(String input) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = digest.digest(input.getBytes());
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
