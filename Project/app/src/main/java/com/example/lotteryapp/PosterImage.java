package com.example.lotteryapp;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * The {@code PosterImage} class handles image uploads to Firebase Storage.
 * It provides the uploaded image's URL via a callback mechanism.
 *
 * @author Team Aurora
 * @version v1
 */
public class PosterImage {

    private final StorageReference storageReference;

    /**
     * Constructor initializes Firebase Storage reference for poster uploads.
     */
    public PosterImage() {
        storageReference = FirebaseStorage.getInstance().getReference("posters");
    }

    /**
     * Uploads the provided image to Firebase Storage and returns the URL via a callback.
     *
     * @param imageUri The URI of the image to upload.
     * @param callback The callback to handle the upload result.
     */
    public void uploadImage(String eventId, Uri imageUri, PosterUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("Image URI is null"));
            return;
        }

        // Generate a unique filename for the image
        StorageReference fileRef = storageReference.child(eventId);

        // Upload the image to Firebase Storage
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                                .addOnFailureListener(callback::onFailure)
                )
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Callback interface for handling image upload success and failure.
     */
    public interface PosterUploadCallback {
        void onSuccess(String imageUrl); // Called with the URL of the uploaded image
        void onFailure(Exception e);    // Called if the upload fails
    }
}
