package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EntrantProfileActivity extends AppCompatActivity {
    private static final String TAG = "EntrantProfileActivity";
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;
    private Entrant entrant;
    private Organizer organizer;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        profilePicture = findViewById(R.id.profile_picture);
        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);

        Intent intent = getIntent();
        entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        organizer = (Organizer) intent.getSerializableExtra("organizer_data");

        if (entrant != null) {
            Log.d(TAG, "Entrant object is not null");
            updateUI(entrant);
        } else {
            Log.e(TAG, "Entrant object is null");
            finish(); // Close the activity if entrant data is not available
        }

        Button editProfileButton = findViewById(R.id.edit_account_button);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEdit = new Intent(EntrantProfileActivity.this, EntrantProfileEditActivity.class);
                intentEdit.putExtra("entrant_data", entrant);
                intentEdit.putExtra("organizer_data", organizer);
                startActivity(intentEdit);
            }
        });
    }

    private void updateUI(Entrant entrant) {
        entrantNameTextView.setText(entrant.getName());
        entrantEmailTextView.setText(entrant.getEmail());
        entrantPhoneTextView.setText(entrant.getPhone());

        if (entrant.getImage_url() != null && !entrant.getImage_url().isEmpty()) {
            Glide.with(this)
                    .load(entrant.getImage_url())
                    .placeholder(R.drawable.ic_profile_photo) // Fallback placeholder image
                    .error(R.drawable.ic_profile_photo) // Fallback error image
                    .circleCrop() // Make image circular
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.ic_profile_photo); // Default placeholder
        }
    }
}