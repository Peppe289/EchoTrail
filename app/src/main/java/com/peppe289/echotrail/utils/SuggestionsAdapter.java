package com.peppe289.echotrail.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter class for displaying a list of city suggestions in a RecyclerView.
 */
public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder> {

    /**
     * List of city suggestions to display.
     */
    private final List<CityProprieties> suggestions;
    /**
     * Listener for handling selection of a suggestion.
     */
    private final OnSuggestionSelectedListener listener;

    /**
     * Constructs a SuggestionsAdapter instance.
     *
     * @param suggestions list of city suggestions
     * @param listener    listener for suggestion selection events
     */
    public SuggestionsAdapter(List<CityProprieties> suggestions, OnSuggestionSelectedListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    /**
     * Inflates the item view and creates a SuggestionViewHolder.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the new view
     * @return a new SuggestionViewHolder
     */
    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new SuggestionViewHolder(view);
    }

    /**
     * Binds data to the given SuggestionViewHolder.
     *
     * @param holder   the ViewHolder to bind data to
     * @param position the position of the data in the list
     */
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position).name;
        double latitude = suggestions.get(position).latitude;
        double longitude = suggestions.get(position).longitude;
        holder.textView.setText(suggestion);
        holder.itemView.setOnClickListener(v -> listener.onSuggestionSelected(suggestion, latitude, longitude));
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return the size of the suggestions list
     */
    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    /**
     * Listener interface for handling city suggestion selection.
     */
    public interface OnSuggestionSelectedListener {
        /**
         * Called when a suggestion is selected.
         *
         * @param suggestion the name of the selected city
         * @param latitude   the latitude of the selected city
         * @param longitude  the longitude of the selected city
         */
        void onSuggestionSelected(String suggestion, double latitude, double longitude);
    }

    /**
     * Data class representing the properties of a city.
     */
    public static class CityProprieties {
        /**
         * Latitude of the city.
         */
        public double latitude;

        /**
         * Longitude of the city.
         */
        public double longitude;

        /**
         * Name of the city.
         */
        public String name;

        /**
         * Constructs a CityProprieties instance.
         *
         * @param name      the name of the city
         * @param latitude  the latitude of the city
         * @param longitude the longitude of the city
         */
        public CityProprieties(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * ViewHolder class for displaying a city suggestion.
     */
    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView displaying the name of the city.
         */
        TextView textView;

        /**
         * Constructs a SuggestionViewHolder instance.
         *
         * @param itemView the item view
         */
        public SuggestionViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
