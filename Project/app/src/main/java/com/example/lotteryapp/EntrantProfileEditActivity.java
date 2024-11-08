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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import java.util.Objects;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";

    private EditText editName, editEmail, editPhone;
    private Button removeProfilePicture, confirmChanges;
    private ImageView currentProfilePicture;
    private ImageButton addProfilePictureButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseFirestore db;
    private String currentUser;
    private StorageReference storageRef;
    private Uri profileImageUri;

    // Declare the ActivityResultLauncher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    LinearProgressIndicator progressIndicator;
    Uri image;
    Button uploadImage;
    ImageButton selectImage;
    ImageView imageView;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    uploadImage.setEnabled(true);
                    image = result.getData().getData();
                    Glide.with(getApplicationContext()).load(image).into(imageView);
                }
            } else {
                Toast.makeText(EntrantProfileEditActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        db = FirebaseFirestore.getInstance();
        FirebaseApp.initializeApp(EntrantProfileEditActivity.this);
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures");

        // Initialize EditText and Buttons
        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        currentUser = intent.getStringExtra("userType");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        removeProfilePicture = findViewById(R.id.remove_profile_picture);
        addProfilePictureButton = findViewById(R.id.add_profile_picture_button);
        currentProfilePicture = findViewById(R.id.profile_photo);
        confirmChanges = findViewById(R.id.confirm_changes);

        if (entrant != null) {
            Log.d(TAG, "Entrant Name: " + entrant.getName());
            Log.d(TAG, "Entrant Email: " + entrant.getEmail());
            Log.d(TAG, "Entrant Phone: " + entrant.getPhone());
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());

            // Check if profileImageUrl is null or empty
            if (entrant.getProfileImageUrl() == null || entrant.getProfileImageUrl().isEmpty()) {
                // Generate avatar with initial if no profile image URL
                Bitmap avatar = AvatarUtils.generateAvatarWithInitial(entrant.getName());
                currentProfilePicture.setImageBitmap(avatar);
            }

            addProfilePictureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    activityResultLauncher.launch(intent);
                }
            });

            confirmChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadImage(image);
                }
            });

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
//            if (entrant != null) {
//                saveEntrantToFirestore(entrant, null); // Set profile image URL to null
//            }
        });
    }

    private void uploadImage(Uri file) {
        StorageReference ref = storageRef.child("images/" + UUID.randomUUID().toString());
        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EntrantProfileEditActivity.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                progressIndicator.setMax(Math.toIntExact(taskSnapshot.getTotalByteCount()));
                progressIndicator.setProgress(Math.toIntExact(taskSnapshot.getBytesTransferred()));
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
        organizer.saveToFirestore(new SaveOrganizerCallback() {
            @Override
            public void onSuccess() {
                // Organizer saved successfully, now create/update Entrant
                createOrUpdateEntrant(deviceId, name, email, phone);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EntrantProfileEditActivity.this, "Error saving organizer", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing organizer document", e);
            }
        });
    }

    private void createOrUpdateEntrant(String deviceId, String name, String email, String phone) {
        Entrant entrant;
        if (phone.isEmpty()) {
            entrant = new Entrant(deviceId, name, email, "");
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

                // Get the userRole from the Intent
                String userRole = getIntent().getStringExtra("userRole");

                // Navigate to the appropriate activity based on userRole
                Intent intent;
                if (Objects.equals(currentUser, "organizer")) {
                    intent = new Intent(EntrantProfileEditActivity.this, OrganizerMainPage.class);
                } else { // Default to EntrantEventsActivity if no role or "entrant"
                    intent = new Intent(EntrantProfileEditActivity.this, EntrantsEventsActivity.class);
                }
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
