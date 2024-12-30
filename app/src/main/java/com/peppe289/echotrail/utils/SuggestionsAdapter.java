package com.peppe289.echotrail.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder> {

    public static class CityProprieties {
        public double latitude;
        public double longitude;
        public String name;

        public CityProprieties(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private List<CityProprieties> suggestions;
    private OnSuggestionSelectedListener listener;

    public SuggestionsAdapter(List<CityProprieties> suggestions, OnSuggestionSelectedListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position).name;
        double latitude = suggestions.get(position).latitude;
        double longitude = suggestions.get(position).longitude;
        holder.textView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> listener.onSuggestionSelected(suggestion, latitude, longitude));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public SuggestionViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnSuggestionSelectedListener {
        void onSuggestionSelected(String suggestion, double latitude, double longitude);
    }
}
