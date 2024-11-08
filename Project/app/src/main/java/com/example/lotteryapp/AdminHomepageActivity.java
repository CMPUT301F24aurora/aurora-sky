package com.example.lotteryapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdminHomepageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view);

        Button viewEvents = findViewById(R.id.admin_v_ev);
        Button viewFacilities = findViewById(R.id.admin_v_fac);
        Button viewProfiles = findViewById(R.id.admin_v_pro);

        viewEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditEventsActivity.class);
                startActivity(intent);
                } });
        viewFacilities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditFacilitiesActivity.class);
                startActivity(intent);
                } });
        viewProfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomepageActivity.this, AdminViewEditProfilesActivity.class);
                startActivity(intent);
                } });
    }
}
