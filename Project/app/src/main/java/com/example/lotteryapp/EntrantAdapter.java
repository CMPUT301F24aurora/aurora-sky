package com.example.lotteryapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private final EntrantClickListener clickListener;
    private FirebaseFirestore db;
    private Context context;

    public interface EntrantClickListener {
        void onEntrantClick(Entrant entrant);
    }

    public EntrantAdapter(List<Entrant> entrantList, EntrantClickListener clickListener) {
        this.entrantList = entrantList;
        this.filteredEntrantList = new ArrayList<>(entrantList);
        this.clickListener = clickListener;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        //Entrant entrant = filteredEntrantList.get(position);
        Entrant entrant = entrantList.get(position);
        holder.entrantName.setText(entrant.getName());
        // Fetch and display the profile picture
        db.collection("entrants").document(entrant.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("image_url");
                        if (imageUrl != null) {
                            Glide.with(context) .load(imageUrl) .into(holder.profilePictureImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> { // Handle any errors
            Log.w("EntrantAdapter", "Error getting document", e); });
        holder.itemView.setOnClickListener(v -> clickListener.onEntrantClick(entrant));
    }

    @Override
    public int getItemCount() {
        //return filteredEntrantList.size();
        return entrantList.size();
    }

    public List<Entrant> filter(String query) {
        filteredEntrantList.clear();
        for (Entrant entrant : entrantList) {
            if (entrant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredEntrantList.add(entrant);
            }
        }
        return filteredEntrantList;
    }

    public void updateList(List<Entrant> newEntrantList) {
        entrantList.clear();
        entrantList.addAll(newEntrantList);
        filteredEntrantList = new ArrayList<>(entrantList); // Ensure proper copy
        notifyDataSetChanged(); // Show all entrants initially
        }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;
        ImageView profilePictureImageView;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.profile_name_value);
            profilePictureImageView = itemView.findViewById(R.id.profile_picture);
        }
    }
}
