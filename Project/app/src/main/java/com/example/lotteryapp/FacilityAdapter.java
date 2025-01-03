package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code FacilityAdapter} class is an adapter for displaying a list of facilities in a RecyclerView.
 * It provides filtering capabilities and handles click events for each facility item.
 *
 * @see RecyclerView
 * @see Facility
 * @version v1
 * @since v1
 * @see FacilityClickListener
 * @see List
 * @see ViewGroup
 * @see LayoutInflater
 */

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {

    private List<Facility> facilityList;
    private List<Facility> filteredFacilityList;
    private FacilityClickListener clickListener;

    public interface FacilityClickListener {
        void onFacilityClick(Facility facility);
    }

    /**
     * Constructs a {@code FacilityAdapter} with the specified list of facilities and click listener.
     *
     * @param facilityList      the list of {@code Facility} objects to be displayed
     * @param clickListener     the {@code FacilityClickListener} for item click events
     */

    public FacilityAdapter(List<Facility> facilityList, FacilityClickListener clickListener) {
        this.facilityList = facilityList;
        this.filteredFacilityList = new ArrayList<>(facilityList);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FacilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.facility_item, parent, false);
        return new FacilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityViewHolder holder, int position) {
        Facility facility = filteredFacilityList.get(position);
        holder.facilityName.setText(facility.getName());
        holder.facilityLocation.setText(facility.getLocation());
        holder.itemView.setOnClickListener(v -> clickListener.onFacilityClick(facility));
    }

    public void updateData(List<Facility> newFacilityList) {
        this.facilityList.clear();
        this.facilityList.addAll(newFacilityList);
        this.filteredFacilityList = new ArrayList<>(newFacilityList);  // Reset filtered list
        notifyDataSetChanged();  // Notify adapter that the data has changed
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                FilterResults results = new FilterResults();

                if (query.isEmpty()) {
                    results.values = facilityList;
                } else {
                    List<Facility> filtered = new ArrayList<>();
                    for (Facility facility : facilityList) {
                        if (facility.getName().toLowerCase().contains(query)) {
                            filtered.add(facility);
                        }
                    }
                    results.values = filtered;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredFacilityList = (List<Facility>) results.values;
                notifyDataSetChanged();  // Refresh the RecyclerView
            }
        };
    }

    /**
     * Returns the total number of filtered items in the data set held by the adapter.
     *
     * @return the number of filtered items in the data set
     */

    @Override
    public int getItemCount() {
        return filteredFacilityList.size();
    }

    public static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView facilityName;
        TextView facilityLocation;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityName = itemView.findViewById(R.id.facility_name);
            facilityLocation = itemView.findViewById(R.id.facility_loc);
        }
    }
}
