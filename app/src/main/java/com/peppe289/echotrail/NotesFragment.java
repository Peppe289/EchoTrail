package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        LinearLayout cardContainer = view.findViewById(R.id.card_container);

        String[] titles = {"Titolo 1", "Titolo 2", "Titolo 3", "Titolo 1", "Titolo 2", "Titolo 3", "Titolo 1", "Titolo 2", "Titolo 3"};
        String[] descriptions = {"Descrizione 1", "Descrizione 2", "Descrizione 3", "Descrizione 1", "Descrizione 2", "Descrizione 3", "Descrizione 1", "Descrizione 2", "Descrizione 3"};

        for (int i = 0; i < titles.length; i++) {
            View card = LayoutInflater.from(requireContext()).inflate(R.layout.card_item, cardContainer, false);

            ViewHolder viewHolder = new ViewHolder(card);
            viewHolder.title.setText(titles[i]);
            viewHolder.description.setText(descriptions[i]);

            cardContainer.addView(card);
        }

        return view;
    }

    static class ViewHolder {
        TextView title, description;

        public ViewHolder(View cardView) {
            this.title = cardView.findViewById(R.id.authors);
            this.description = cardView.findViewById(R.id.content);
        }
    }
}