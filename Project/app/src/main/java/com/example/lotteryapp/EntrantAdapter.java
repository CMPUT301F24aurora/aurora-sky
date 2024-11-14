package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private final EntrantClickListener clickListener;

    public interface EntrantClickListener {
        void onEntrantClick(Entrant entrant);
    }

    public EntrantAdapter(List<Entrant> entrantList, EntrantClickListener clickListener) {
        this.entrantList = entrantList;
        this.filteredEntrantList = new ArrayList<>(entrantList);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        //Entrant entrant = filteredEntrantList.get(position);
        Entrant entrant = entrantList.get(position);
        holder.entrantName.setText(entrant.getName());
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

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.profile_name_value);
        }
    }
}
