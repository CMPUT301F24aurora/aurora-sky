package com.example.lotteryapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AdminViewEditFacilitiesActivity extends AppCompatActivity implements FacilityAdapter.FacilityClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_facilities_page);
    }

    @Override
    public void onFacilityClick(Facility facility) {

    }
}
