package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantProfileEditActivity extends AppCompatActivity {

    private static final String TAG = "EntrantProfileEditActivity";

    private EditText editName, editEmail, editPhone;
    private Button updateProfilePicture, confirmChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_profile_edit);

        // Initialize EditText and Buttons
        Intent intent = getIntent();
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        updateProfilePicture = findViewById(R.id.update_profile_picture);
        confirmChanges = findViewById(R.id.confirm_changes);

        if(entrant != null){
            editName.setText(entrant.getName());
            editEmail.setText(entrant.getEmail());
            editPhone.setText(entrant.getPhone());
        }

        // OnClickListener for Confirm Changes Button
        confirmChanges.setOnClickListener(v -> saveEntrantDetails());
    }

    private void saveEntrantDetails() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the device ID
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Create Entrant object
        Entrant entrant;
        if (phone.isEmpty()) {
            entrant = new Entrant(deviceId, name, email);
        } else {
            entrant = new Entrant(deviceId, name, email, phone);
        }

        // Save Entrant to Firestore
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
