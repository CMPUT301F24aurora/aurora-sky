package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminViewFacilitiesContent extends AppCompatActivity {

    private TextView nameTextView;
    private TextView timeTextView;
    private TextView locationTextView;
    private TextView emailTextView;
    private Button removeButton;
    private FirebaseFirestore db;
    private String facilityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_facility_page);

        nameTextView = findViewById(R.id.admin_fac_name_view);
        timeTextView = findViewById(R.id.admin_fac_time_view);
        locationTextView = findViewById(R.id.admin_fac_loc_view);
        emailTextView = findViewById(R.id.admin_fac_email_view);
        removeButton = findViewById(R.id.admin_fac_remove_button);

        db = FirebaseFirestore.getInstance();

        // Retrieve facility details from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("facilityName");
        String time = intent.getStringExtra("facilityTime");
        String location = intent.getStringExtra("facilityLocation");
        String email = intent.getStringExtra("facilityEmail");
        facilityId = intent.getStringExtra("facilityId");

        // Populate TextViews with facility details
        nameTextView.setText(name);
        timeTextView.setText(time);
        locationTextView.setText(location);
        emailTextView.setText(email);

        // Set up the remove button click listener
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFacility();
            }
        });
    }

    private void deleteFacility() {
        CollectionReference facilitiesRef = db.collection("facilities");
        facilitiesRef.document(facilityId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminViewFacilitiesContent.this, "Facility deleted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to AdminViewEditFacilitiesActivity
                    Intent intent = new Intent(AdminViewFacilitiesContent.this, AdminViewEditFacilitiesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminViewFacilitiesContent.this, "Error deleting facility: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
