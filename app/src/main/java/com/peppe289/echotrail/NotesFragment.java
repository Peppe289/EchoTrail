package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.notes.NotesDAO;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

    private FragmentNotesBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        binding = FragmentNotesBinding.inflate(inflater, container, false);

        LinearLayout cardContainer = view.findViewById(R.id.card_container);

        UserDAO.getReadedNotesList(document -> {
            if (document == null) {
                TextView text = new TextView(binding.getRoot().getContext());
                text.setText("Nessuna nota presente");
                text.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                layoutParams.gravity = Gravity.CENTER;
                text.setLayoutParams(layoutParams);

                cardContainer.addView(text);
                return;
            }

            String uid = document.getString("userId");
            String city = document.getString("city");
            String description = document.getString("content");
            Timestamp timestamp = (Timestamp) document.get("timestamp");
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            View card = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.card_item, cardContainer, false);

            ViewHolder viewHolder = new ViewHolder(card);
            viewHolder.title.setText(uid);
            viewHolder.description.setText(description);
            viewHolder.city.setText(city);
            viewHolder.date.setText(formattedDate);

            cardContainer.addView(card);
        });
        return view;
    }

    static class ViewHolder {
        TextView title, description, city, date;

        public ViewHolder(View cardView) {
            this.title = cardView.findViewById(R.id.authors);
            this.description = cardView.findViewById(R.id.content);
            this.city = cardView.findViewById(R.id.city);
            this.date = cardView.findViewById(R.id.date);
        }
    }
}