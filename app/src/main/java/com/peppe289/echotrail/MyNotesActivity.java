package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityMyNotesBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyNotesActivity extends AppCompatActivity {
    private ActivityMyNotesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout cardContainer = findViewById(R.id.card_container);

        String[] titles = {"Titolo 1", "Titolo 2", "Titolo 3", "Titolo 1", "Titolo 2", "Titolo 3", "Titolo 1", "Titolo 2", "Titolo 3"};
        String[] descriptions = {"Descrizione 1", "Descrizione 2", "Descrizione 3", "Descrizione 1", "Descrizione 2", "Descrizione 3", "Descrizione 1", "Descrizione 2", "Descrizione 3"};


        UserController.getUserNotesList(document -> {
            String city = document.getString("city");
            String description = document.getString("content");
            Timestamp timestamp = (Timestamp) document.get("timestamp");
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            View card = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.card_item, cardContainer, false);

            ViewHolder viewHolder = new ViewHolder(card);
            viewHolder.title.setText("La tua nota");
            viewHolder.description.setText(description);
            viewHolder.city.setText(city);
            viewHolder.date.setText(formattedDate);

            cardContainer.addView(card);
        });
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
