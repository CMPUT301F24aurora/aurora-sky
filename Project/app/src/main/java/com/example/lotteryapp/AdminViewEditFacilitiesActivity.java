package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

/**
 * The AdminViewEditFacilitiesActivity class allows admin users to view and edit facilities.
 * This activity displays a list of facilities and provides search functionality.
 *
 * @see AppCompatActivity
 * @see FacilityAdapter
 * @see Facility
 * @see FirebaseFirestore
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminViewEditFacilitiesActivity extends AppCompatActivity implements FacilityAdapter.FacilityClickListener {

    private RecyclerView adminFacList;
    private FacilityAdapter facilityAdapter;
    private List<Facility> facilityList;
    private List<Facility> filteredFacilityList;
    private FirebaseFirestore db;
    private TextView noFacilitiesText;
    private DBManagerEvent dbManagerEvent;

    /**
     * Called when the activity is first created.
     * This method sets up the layout, initializes the RecyclerView, and loads facilities from the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_facilities_page);

        //Add mock facility:
        /*
        Facility facility = new Facility("12345", "test", "12:50", "13:50", "Edmonton", "testing@gmail.com");
        facility.saveToFirestore(new Facility.FacilityCallback() {

            @Override
            public void onSuccess() {
                Log.d("AddMockFaculty", "Faculty created successfully!");
            }

            @Override
            public void onFailure(Exception e) {
                Log.w("AddMockFaculty", "Error creating Faculty!");
            }
        });
         */

        adminFacList = findViewById(R.id.admin_fac_list);
        noFacilitiesText = findViewById(R.id.admin_no_facilities_text);
        db = FirebaseFirestore.getInstance();
        facilityList = new ArrayList<>();
        facilityAdapter = new FacilityAdapter(facilityList, this);

        dbManagerEvent = new DBManagerEvent();

        adminFacList.setLayoutManager(new LinearLayoutManager(this));
        adminFacList.setAdapter(facilityAdapter);

        setupSearchView();
        loadFacilities();
    }

    private void setupSearchView() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.admin_fac_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                facilityAdapter.getFilter().filter(query);  // Apply filter on submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                facilityAdapter.getFilter().filter(newText);  // Apply filter on text change
                return false;
            }
        });
    }

    /**
     * Loads facilities from the database and updates the facility list.
     * Retrieves facilities from the "facilities" collection in Firestore and adds them to the facility list.
     */
    private void loadFacilities() {
        CollectionReference facilitiesRef = db.collection("facilities");
        /**
         * Called when the task to retrieve facilities is complete.
         *
         * @param task the task to retrieve facilities
         */
        facilitiesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Facility> facilities = task.getResult().toObjects(Facility.class);
                    facilityList.clear();
                    facilityList.addAll(facilities);
                    filteredFacilityList = new ArrayList<>(facilities);  // Reset filtered list with fresh data
                    if (facilityList.isEmpty()) {
                        noFacilitiesText.setVisibility(View.VISIBLE);
                        adminFacList.setVisibility(View.GONE);
                    } else {
                        noFacilitiesText.setVisibility(View.GONE);
                        adminFacList.setVisibility(View.VISIBLE);
                    }
                    facilityAdapter.updateData(facilities);
                    facilityAdapter.notifyDataSetChanged();  // Notify adapter that the data has changed
                } else {
                    Toast.makeText(AdminViewEditFacilitiesActivity.this, "Error getting facilities!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Called when a facility is clicked.
     * Starts the AdminViewFacilitiesContent activity and passes the facility details to it.
     *
     * @param facility the clicked facility
     * @see AdminViewFacilitiesContent
     */
    @Override
    public void onFacilityClick(Facility facility) {
        // Create an Intent to start the new activity
        Intent intent = new Intent(this, AdminViewFacilitiesContent.class);

        // Pass necessary data to the new activity
        intent.putExtra("facilityName", facility.getName());
        intent.putExtra("facilityLocation", facility.getLocation());
        intent.putExtra("facilityTime", facility.getStartTime());
        intent.putExtra("facilityEmail", facility.getEmail());
        intent.putExtra("facilityId", facility.getOrganizerId());
        startActivity(intent);
    }
}
