//package com.example.lotteryapp;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//
//public class ProfileActivity extends Activity {
//
//    private ImageView profileImageView;
//    private ProfileImage profileImage;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_profile);
//
//        profileImageView = findViewById(R.id.profile_image);
//        profileImage = new ProfileImage();
//
//        // Generate and save the profile picture
////        profileImage.generateAndSaveProfilePicture("John Doe", "john",
////                new OnSuccessListener<String>() {
////                    @Override
////                    public void onSuccess(String downloadUrl) {
////                        Log.d("ProfileActivity", "Image uploaded successfully. Download URL: " + downloadUrl);
////                    }
////                },
////                new OnFailureListener() {
////                    @Override
////                    public void onFailure(@NonNull Exception exception) {
////                        Log.e("ProfileActivity", "Image upload failed: " + exception.getMessage());
////                    }
////                });
//    }
//}
