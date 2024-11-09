package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.FacilityViewHolder> {

    private List<Facility> facilityList;
    private List<Facility> filteredFacilityList;
    private FacilityClickListener clickListener;

    public interface FacilityClickListener {
        void onFacilityClick(Facility facility);
    }

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
        Facility facility = facilityList.get(position);
        holder.facilityName.setText(facility.getName());
        holder.itemView.setOnClickListener(v -> clickListener.onFacilityClick(facility));
    }

    public void filter(String query) {
        filteredFacilityList.clear();
        if (query.isEmpty()) {
            filteredFacilityList.addAll(facilityList);
        }
        else {
            for (Facility facility : facilityList) {
                if (facility.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFacilityList.add(facility);
                }
            }
        } notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredFacilityList.size();
    }

    public static class FacilityViewHolder extends RecyclerView.ViewHolder {
        TextView facilityName;

        public FacilityViewHolder(@NonNull View itemView) {
            super(itemView);
            facilityName = itemView.findViewById(R.id.facility_name);
        }
    }
}
