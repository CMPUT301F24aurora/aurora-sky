package com.example.lotteryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        removeProfilePicture = findViewById(R.id.remove_profile_picture);
        addProfilePictureButton = findViewById(R.id.add_profile_picture_button);
        currentProfilePicture = findViewById(R.id.profile_photo);
        confirmChanges = findViewById(R.id.confirm_changes);

        if(entrant != null){
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());
        }

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
        removeProfilePicture.setOnClickListener(v -> {
            // Clear the profile picture
            profileImageUri = null;
            // Regenerate the profile picture based on the entrant's name
            if (entrant != null) {
                currentProfilePicture.setImageBitmap(generateTextDrawable(entrant.getName()));
            } else {
                currentProfilePicture.setImageResource(R.drawable.ic_profile_photo); // Set default image if no entrant data
            }

            // Remove the image URL from the Entrant object in Firestore
            if (entrant != null) {
                saveEntrantToFirestore(entrant, null); // Set profile image URL to null
            }
        });
    }

    private Bitmap generateTextDrawable(String text) {
        // Create a new Bitmap (400x400px for example)
        int width = 400;
        int height = 400;

        // Create a Bitmap with the required size
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Set the background color (random or fixed based on the name)
        canvas.drawColor(generateBackgroundColor(text));

        // Set up the paint object for text (white, bold, and centered)
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(150f);  // Size of the text
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        // Draw the initial(s) on the canvas (using the first letter of the name)
        String initials = text.length() > 1 ? text.substring(0, 2).toUpperCase() : text.toUpperCase();
        Rect textBounds = new Rect();
        paint.getTextBounds(initials, 0, initials.length(), textBounds);

        // Draw the text in the center of the canvas
        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() / 2f) - (textBounds.exactCenterY());
        canvas.drawText(initials, x, y, paint);

        return bitmap;
    }

    // Helper method to generate a background color based on the entrant's name
    private int generateBackgroundColor(String text) {
        // Generate a hash code from the text (entrant's name) and use it to create a color
        int color = text.hashCode();
        // Ensure the color is a valid RGB color (you can tweak this as per your preference)
        return Color.argb(255, Math.abs(color % 256), Math.abs((color / 256) % 256), Math.abs((color / 65536) % 256));
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Entrant entrant, Uri imageUri) {
        StorageReference fileRef = storageRef.child(entrant.getId() + ".jpg");
        String entrantId = entrant.getId();
        if (entrantId == null || entrantId.isEmpty()) {
            Log.e(TAG, "Entrant ID is null or empty");
            return;  // Stop the upload process
        }
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
        String profilepicture = "";


        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create Entrant object
        Entrant entrant;
        if (phone.isEmpty()) {
            entrant = new Entrant(deviceId, name, email, ""); // Use empty string for profile picture if none
        } else {
            entrant = new Entrant(deviceId, name, email, phone, "");
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
        } else {
            entrant.setProfileImageUrl(null); // Explicitly set profile image URL to null
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

}
