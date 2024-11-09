package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import android.widget.SearchView;

public class AdminViewEditFacilitiesActivity extends AppCompatActivity implements FacilityAdapter.FacilityClickListener {

    private RecyclerView adminFacList;
    private FacilityAdapter facilityAdapter;
    private List<Facility> facilityList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_facilities_page);

        //Add mock facility:
        /*
        Facility facility = new Facility("test", "12:50", "Edmonton", "testing@gmail.com");
        facility.saveToFirestore(new Facility.FacilityCallback() {
            @Override
            public void onSuccess(String documentId) {
                facility.setId(documentId);
                Log.d("AddMockFaculty", "Faculty created successfully!");
            }

            @Override
            public void onFailure(Exception e) {
                Log.w("AddMockFaculty", "Error creating Faculty!");
            }
        });
         */

        adminFacList = findViewById(R.id.admin_fac_list);
        db = FirebaseFirestore.getInstance();
        facilityList = new ArrayList<>();
        facilityAdapter = new FacilityAdapter(facilityList, this);

        adminFacList.setLayoutManager(new LinearLayoutManager(this));
        adminFacList.setAdapter(facilityAdapter);

        loadFacilities();

        // Set up SearchView
        SearchView searchView = findViewById(R.id.fsearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            facilityAdapter.filter(query); return false;
        }
        @Override
        public boolean onQueryTextChange(String newText) {
            facilityAdapter.filter(newText);
            return false;
        }
        });
    }

    private void loadFacilities() {
        CollectionReference facilitiesRef = db.collection("facilities");
        facilitiesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Facility facility = document.toObject(Facility.class);
                        facilityList.add(facility);
                    }
                    facilityAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminViewEditFacilitiesActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onFacilityClick(Facility facility) {
        // Create an Intent to start the new activity
        Intent intent = new Intent(this, AdminViewFacilitiesContent.class);

        // Pass necessary data to the new activity if needed
        intent.putExtra("facilityName", facility.getName());
        intent.putExtra("facilityLocation", facility.getLocation());
        intent.putExtra("facilityTime", facility.getStartTime());
        intent.putExtra("facilityEmail", facility.getEmail());
        intent.putExtra("facilityId", facility.getOrganizerId());
        startActivity(intent);
    }
}
