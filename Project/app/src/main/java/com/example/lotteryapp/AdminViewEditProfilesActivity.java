package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import android.widget.SearchView;

/**
 * The AdminViewEditProfilesActivity class allows admin users to view and edit entrant profiles.
 * This activity displays a list of entrants and provides search functionality.
 *
 * @see AppCompatActivity
 * @see EntrantAdapter
 * @see Entrant
 * @see FirebaseFirestore
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminViewEditProfilesActivity extends AppCompatActivity implements EntrantAdapter.EntrantClickListener {

    private RecyclerView recyclerViewEntrants;
    private EntrantAdapter entrantAdapter;
    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * This method sets up the layout, initializes the RecyclerView, and loads entrants from the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_entrants);

        recyclerViewEntrants = findViewById(R.id.ev_entrants_lv);
        db = FirebaseFirestore.getInstance();
        entrantList = new ArrayList<>();
        entrantAdapter = new EntrantAdapter(entrantList, this);

        recyclerViewEntrants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEntrants.setAdapter(entrantAdapter);

        loadEntrants();

        // Set up SearchView
        SearchView searchView = findViewById(R.id.entrants_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Called when a query is submitted in the SearchView.
             *
             * @param query the search query
             * @return false to indicate the query has been handled
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                //recyclerViewEntrants.setAdapter();
                filteredEntrantList = entrantAdapter.filter(query);
                entrantList.clear();
                entrantList.addAll(filteredEntrantList);
                entrantAdapter.notifyDataSetChanged();
                return false;
            }

            /**
             * Called when the query text is changed in the SearchView.
             *
             * @param newText the new text in the SearchView
             * @return false to indicate the query text change has been handled
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.trim().isEmpty()) {
                    filteredEntrantList = entrantAdapter.filter(newText);
                    entrantList.clear();
                    entrantList.addAll(filteredEntrantList);
                    entrantAdapter.notifyDataSetChanged();
                }
                else {
                    loadEntrants(); // Reload data from Firestore when query is cleared
                }
                return false;
            }
        });
    }

    /**
     * Loads entrants from the database and updates the entrant list.
     * Retrieves entrants from the "entrants" collection in Firestore and adds them to the entrant list.
     */
    private void loadEntrants() {
        CollectionReference entrantsRef = db.collection("entrants");
        /**
         * Called when the task to retrieve entrants is complete.
         *
         * @param task the task to retrieve entrants
         */
        entrantsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Entrant entrant = document.toObject(Entrant.class);
                        entrantList.add(entrant);
                    }
                    //entrantAdapter.updateList(entrantList);
                    entrantAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminViewEditProfilesActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
        entrantsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(AdminViewEditProfilesActivity.this, "Error getting documents: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                } if (snapshots != null) {
                    entrantList.clear();
                    for (DocumentSnapshot document : snapshots.getDocuments()) {
                        Entrant entrant = document.toObject(Entrant.class);
                        entrantList.add(entrant);
                    }
                    entrantAdapter.notifyDataSetChanged();
                }
            }
        });

         */
    }

    /**
     * Called when an entrant is clicked.
     * Starts the AdminViewProfilesContent activity and passes the entrant details to it.
     *
     * @param entrant the clicked entrant
     * @see AdminViewProfilesContent
     */
    @Override
    public void onEntrantClick(Entrant entrant) {
        Intent intent = new Intent(this, AdminViewProfilesContent.class);
        intent.putExtra("entrantName", entrant.getName());
        intent.putExtra("entrantId", entrant.getId());
        intent.putExtra("entrantEmail", entrant.getEmail());
        intent.putExtra("entrantPhone", entrant.getPhone());
        intent.putExtra("entrantImage", entrant.getImage_url());
        startActivity(intent);
    }
}

