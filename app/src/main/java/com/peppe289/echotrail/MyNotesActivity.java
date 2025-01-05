package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
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
        setUpToolBar();

        UserController.getUserNotesList(querySnapshot -> {
            if (querySnapshot == null || querySnapshot.isEmpty()) {
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
            for (DocumentSnapshot document : querySnapshot) {
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
            }
        });
    }

    private void setUpToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
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
