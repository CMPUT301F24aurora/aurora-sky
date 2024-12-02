package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class EntrantProfileActivity extends AppCompatActivity {
    // Existing fields
    private TextView entrantNameTextView;
    private TextView entrantEmailTextView;
    private TextView entrantPhoneTextView;
    private Entrant entrant;
    private Organizer organizer;
    private ImageView profilePicture;

    // Add reference for Switch
    private Switch notificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile);

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        entrantNameTextView = findViewById(R.id.profile_name_value);
        entrantEmailTextView = findViewById(R.id.profile_email_value);
        entrantPhoneTextView = findViewById(R.id.profile_phone_value);
        notificationSwitch = findViewById(R.id.notification_switch);  // Initialize the Switch

        // Existing code to get data
        Intent intent = getIntent();
        entrant = (Entrant) intent.getSerializableExtra("entrant_data");
        organizer = (Organizer) intent.getSerializableExtra("organizer_data");

        if (entrant != null) {
            updateUI(entrant);
        } else {
            finish();  // Handle missing data
        }

        // Listener for Switch
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Logic when notifications are enabled
                    entrant.setNotificationAllowed(true);
                    Toast.makeText(EntrantProfileActivity.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Logic when notifications are disabled
                    entrant.setNotificationAllowed(false);
                    Toast.makeText(EntrantProfileActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
                }
                // Optionally, save the state to preferences or a database
                DatabaseManager.saveEntrant(entrant, new SaveEntrantCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("","profile updated");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.w("", "Error writing entrant document", e);
                    }
                });
            }
        });

        // Edit profile button logic
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
        notificationSwitch.setChecked(entrant.getNotificationAllowed());

        // Load profile picture using Glide
        if (entrant.getImage_url() != null && !entrant.getImage_url().isEmpty()) {
            Glide.with(this)
                    .load(entrant.getImage_url())
                    .placeholder(R.drawable.ic_profile_photo)
                    .error(R.drawable.ic_profile_photo)
                    .circleCrop()
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.ic_profile_photo);
        }
    }
}
