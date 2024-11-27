package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ProfileImage {

    private static final String TAG = "ProfileImage";

    // Generate an arbitrary picture for the given name
    public Bitmap generateArbitraryPicture(String name) {
        int size = 200; // Size of the profile image
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Generate background color
        int backgroundColor = generateRandomColor();
        canvas.drawColor(backgroundColor);

        // Draw the first letter of the name
        String firstLetter = name.substring(0, 1).toUpperCase();
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80); // Adjust text size as needed
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        // Center the text
        Rect bounds = new Rect();
        textPaint.getTextBounds(firstLetter, 0, firstLetter.length(), bounds);
        int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
        int y = (canvas.getHeight() / 2) + (bounds.height() / 2);

        canvas.drawText(firstLetter, x, y, textPaint);

        return bitmap;
    }

    // Helper method to generate a random color
    private int generateRandomColor() {
        return Color.rgb(
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255)
        );
    }

    // Save or upload the provided URI image or generate a default image if none exists
    public void uploadImageToFirebase(String entrantId, Uri imageUri, String name, ProfileImageCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef;

        if (imageUri != null) {
            // Store user-uploaded images in the profile-images/ folder
            imageRef = storageRef.child("profile-images/" + entrantId + ".png");
            uploadImageFromUri(imageUri, imageRef, callback);
        } else {
            // Store generated images in the generated-images/ folder
            imageRef = storageRef.child("generated-images/" + entrantId + ".png");
            Bitmap generatedBitmap = generateArbitraryPicture(name);
            uploadGeneratedImage(imageRef, generatedBitmap, callback);
        }
    }

    // Upload a user-selected URI image to Firebase Storage
    private void uploadImageFromUri(Uri imageUri, StorageReference imageRef, ProfileImageCallback callback) {
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "Image uploaded successfully: " + uri.toString());
                    callback.onSuccess(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // Upload a generated Bitmap to Firebase Storage
    private void uploadGeneratedImage(StorageReference imageRef, Bitmap bitmap, ProfileImageCallback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "Generated image upload failed: " + e.getMessage());
            callback.onFailure(e);
        }).addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d(TAG, "Generated image uploaded successfully: " + uri.toString());
                callback.onSuccess(uri.toString());
            });
        });
    }

    public interface ProfileImageCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }
}
