package com.example.lotteryapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for managing and displaying Entrant objects in a RecyclerView.
 * This adapter supports filtering and click events on entrant items.
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private final EntrantClickListener clickListener;
    private Context context;

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
     * @param context The context in which the adapter is being used.
     * @param entrantList List of Entrant objects to display.
     * @param clickListener Listener for Entrant item click events.
     */
    public EntrantAdapter(Context context, List<Entrant> entrantList, EntrantClickListener clickListener) {
        this.context = context;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return filteredEntrantList.size();
    }

    /**
     * Binds the Entrant data to the ViewHolder.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = filteredEntrantList.get(position);
        Log.d("EntrantAdapter", "Displaying: " + entrant.getName());
        holder.entrantName.setText(entrant.getName());
        String entrantPhoto = entrant.getImage_url();
        if (entrantPhoto != null && !entrantPhoto.isEmpty()) {
            Glide.with(context)
                    .load(entrantPhoto)
                    .placeholder(R.drawable.ic_profile_photo) // Fallback placeholder image
                    .error(R.drawable.ic_profile_photo) // Fallback error image
                    .circleCrop() // Make image circular
                    .into(holder.profilePicture);
        }
        else {
            holder.profilePicture.setImageResource(R.drawable.ic_profile_photo); // Default placeholder
        }
        holder.itemView.setOnClickListener(v -> clickListener.onEntrantClick(entrant));
    }

    /**
     * Updates the adapter's data set with a new list of Entrants.
     *
     * @param newEntrantList The new list of Entrants to display.
     */
    public void updateData(List<Entrant> newEntrantList) {
        this.entrantList.clear();
        this.entrantList.addAll(newEntrantList);
        this.filteredEntrantList = new ArrayList<>(newEntrantList);
        notifyDataSetChanged();
    }

    /**
     * Returns a Filter that can be used to constrain data with a filtering pattern.
     *
     * @return A Filter for constraining data in the adapter.
     */
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                FilterResults results = new FilterResults();

                if (query.isEmpty()) {
                    results.values = entrantList;
                } else {
                    List<Entrant> filtered = new ArrayList<>();
                    for (Entrant entrant : entrantList) {
                        if (entrant.getName().toLowerCase().contains(query)) {
                            filtered.add(entrant);
                        }
                    }
                    results.values = filtered;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredEntrantList = (List<Entrant>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * ViewHolder class for Entrant items.
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;
        private ImageView profilePicture;

        /**
         * Constructor for EntrantViewHolder.
         *
         * @param itemView The View that represents an Entrant item.
         */
        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.admin_ent_name);
            profilePicture = itemView.findViewById(R.id.admin_ent_photo);
        }
    }
}