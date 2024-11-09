package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class OrganizerFacilityListActivity extends AppCompatActivity implements FacilityAdapter.FacilityClickListener {

    private RecyclerView facilityRecyclerView;
    private FacilityAdapter facilityAdapter;
    private List<Facility> facilityList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_list);

        facilityRecyclerView = findViewById(R.id.facility_recycler_view);
        facilityRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        facilityAdapter = new FacilityAdapter(facilityList, this);
        facilityRecyclerView.setAdapter(facilityAdapter);

        loadFacilities();
    }

    private void loadFacilities() {
        facilityList.clear();

        if (facilityList.isEmpty()) {
            Toast.makeText(this, "You don’t have any facilities.", Toast.LENGTH_SHORT).show();
        } else {
            facilityAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFacilityClick(Facility facility) {
        Intent intent = new Intent(OrganizerFacilityListActivity.this, OrganizerFacilityActivity.class);
        intent.putExtra("facility_data", facility);
        startActivity(intent);
    }
}
