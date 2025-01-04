package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.peppe289.echotrail.dao.notes.NotesDAO;
import com.peppe289.echotrail.databinding.ActivityReadNotesBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReadNotesActivity extends AppCompatActivity {

    private ActivityReadNotesBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> notesID = getIntent().getStringArrayListExtra("notes");

        binding = ActivityReadNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout cardContainer = findViewById(R.id.card_container);

        NotesDAO.getNotes(notesID, document -> {
            String username = document.getString("username");
            if (username == null || username.isEmpty()) {
                username = "Anonimo";
            }
            String city = document.getString("city");
            String description = document.getString("content");
            Timestamp timestamp = (Timestamp) document.get("timestamp");
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            View card = LayoutInflater.from(binding.getRoot().getContext()).inflate(R.layout.card_item, cardContainer, false);

            MyNotesActivity.ViewHolder viewHolder = new MyNotesActivity.ViewHolder(card);
            viewHolder.title.setText(username);
            viewHolder.description.setText(description);
            viewHolder.city.setText(city);
            viewHolder.date.setText(formattedDate);

            cardContainer.addView(card);
        });
    }
}
