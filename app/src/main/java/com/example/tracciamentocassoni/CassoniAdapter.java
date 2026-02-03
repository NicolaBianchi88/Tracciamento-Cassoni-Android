package com.example.tracciamentocassoni;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CassoniAdapter extends RecyclerView.Adapter<CassoniAdapter.CassoneViewHolder> {

    private final Set<String> selectedCassoni = new HashSet<>();
    private List<String> cassoni = new ArrayList<>();

    public void setCassoni(List<String> list) {
        this.cassoni = list != null ? list : new ArrayList<>();
        selectedCassoni.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedCassoni() {
        return new HashSet<>(selectedCassoni);
    }

    @NonNull
    @Override
    public CassoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cassone, parent, false);
        return new CassoneViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CassoneViewHolder holder, int position) {
        String cassoneCode = cassoni.get(position);
        holder.textCassoneCode.setText(cassoneCode);

        // Imposta stato visivo sul contenitore del background
        holder.bgContainer.setSelected(selectedCassoni.contains(cassoneCode));

        holder.itemView.setOnClickListener(v -> {
            if (selectedCassoni.contains(cassoneCode)) {
                selectedCassoni.remove(cassoneCode);
            } else {
                selectedCassoni.add(cassoneCode);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cassoni.size();
    }

    public List<String> getAllCassoni() {
        return new ArrayList<>(cassoni);
    }

    static class CassoneViewHolder extends RecyclerView.ViewHolder {
        TextView textCassoneCode;
        View bgContainer;

        CassoneViewHolder(View itemView) {
            super(itemView);
            textCassoneCode = itemView.findViewById(R.id.textCassoneCode);
            bgContainer = itemView.findViewById(R.id.bgContainer);
        }
    }
}
