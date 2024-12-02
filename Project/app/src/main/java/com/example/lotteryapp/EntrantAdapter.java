package com.example.lotteryapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private final EntrantClickListener clickListener;
    private Context context;

    public interface EntrantClickListener {
        void onEntrantClick(Entrant entrant);
    }

    public EntrantAdapter(Context context, List<Entrant> entrantList, EntrantClickListener clickListener) {
        this.context = context;
        this.entrantList = entrantList;
        this.filteredEntrantList = new ArrayList<>(entrantList);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_entrant_card, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return filteredEntrantList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = filteredEntrantList.get(position);
        Log.d("EntrantAdapter", "Displaying: " + entrant.getName());
        holder.entrantName.setText(entrant.getName());
//        Glide.with(context).load(entrant.getImage_url()).into(holder.admin_ent_photo);
        holder.itemView.setOnClickListener(v -> clickListener.onEntrantClick(entrant));
    }

    public void updateData(List<Entrant> newEntrantList) {
        this.entrantList.clear();
        this.entrantList.addAll(newEntrantList);
        this.filteredEntrantList = new ArrayList<>(newEntrantList);  // Reset filtered list
        notifyDataSetChanged();  // Notify adapter that the data has changed
    }

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
                notifyDataSetChanged();  // Refresh the RecyclerView
            }
        };
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView entrantName;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            entrantName = itemView.findViewById(R.id.admin_ent_name);
        }
    }
}