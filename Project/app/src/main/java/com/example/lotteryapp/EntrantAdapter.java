package com.example.lotteryapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for managing and displaying Entrant objects in a RecyclerView.
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private final EntrantClickListener clickListener;

    /**
     * Interface for handling click events on Entrant items.
     */
    public interface EntrantClickListener {
        /**
         * Called when an Entrant item is clicked.
         *
         * @param entrant The clicked Entrant object.
         */
        void onEntrantClick(Entrant entrant);
    }

    /**
     * Constructor for EntrantAdapter.
     *
     * @param entrantList List of Entrant objects to display.
     * @param clickListener Listener for Entrant item click events.
     */
    public EntrantAdapter(List<Entrant> entrantList, EntrantClickListener clickListener) {
        this.entrantList = entrantList;
        this.filteredEntrantList = new ArrayList<>(entrantList);
        this.clickListener = clickListener;
    }

    /**
     * Creates new ViewHolder instances for Entrant items.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new EntrantViewHolder that holds a View for an Entrant item.
     */
    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * Binds the Entrant data to the ViewHolder.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);
        Log.d("EntrantAdapter", "Displaying: " + entrant.getName());
        holder.entrantName.setText(entrant.getName());
        holder.itemView.setOnClickListener(v -> clickListener.onEntrantClick(entrant));
    }

    /**
     * Filters the Entrant list based on a query string.
     *
     * @param query The search query to filter Entrants.
     * @return A filtered list of Entrants.
     */
    public List<Entrant> filter(String query) {
        filteredEntrantList.clear();
        for (Entrant entrant : entrantList) {
            if (entrant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredEntrantList.add(entrant);
            }
        }
        return filteredEntrantList;
    }

    /**
     * Updates the Entrant list with new data.
     *
     * @param newEntrantList The new list of Entrants to display.
     */
    public void updateList(List<Entrant> newEntrantList) {
        entrantList.clear();
        entrantList.addAll(newEntrantList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for Entrant items.
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;

        /**
         * Constructor for EntrantViewHolder.
         *
         * @param itemView The View that represents an Entrant item.
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.profile_name_value);
        }
    }
}