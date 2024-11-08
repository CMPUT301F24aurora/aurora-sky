package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
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

public class AdminViewEditProfilesActivity extends AppCompatActivity implements EntrantAdapter.EntrantClickListener{
    private RecyclerView recyclerViewEntrants;
    private EntrantAdapter entrantAdapter;
    private List<Entrant> entrantList;
    private FirebaseFirestore db;

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
            @Override
            public boolean onQueryTextSubmit(String query) {
                entrantAdapter.filter(query); return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                entrantAdapter.filter(newText);
                return false;
            }
        });
    }

    private void loadEntrants() {
        CollectionReference entrantsRef = db.collection("entrants");
        entrantsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Entrant entrant = document.toObject(Entrant.class);
                        entrantList.add(entrant);
                    }
                    entrantAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminViewEditProfilesActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEntrantClick(Entrant entrant) {
        Intent intent = new Intent(this, AdminViewProfilesContent.class);
        intent.putExtra("entrantName", entrant.getName());
        intent.putExtra("entrantId", entrant.getId());
        intent.putExtra("entrantEmail", entrant.getEmail());
        intent.putExtra("entrantPhone", entrant.getPhone());
        startActivity(intent);
    }
}
