package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.utils.MoveActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final String NOTES_LIST_EMPTY = "Nessuna nota presente";
    private final List<String> notes = new ArrayList<>();
    private FragmentNotesBinding binding;
    private LinearLayout cardContainer;
    private ScheduledFuture<?> scheduledFuture;
    private TextView textListEmpty;

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
        textListEmpty = new TextView(binding.getRoot().getContext());
        cardContainer = view.findViewById(R.id.card_container);
        setUpEmptyListMessage();
        startFetchingReadedNotes();

        return view;
    }

    private void startFetchingReadedNotes() {
        scheduledFuture = executorService.scheduleWithFixedDelay(this::fetchReadedNotes, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
        executorService.shutdown();
    }

    private void setUpEmptyListMessage() {
        textListEmpty.setText(NOTES_LIST_EMPTY);
        textListEmpty.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.gravity = Gravity.CENTER;
        textListEmpty.setLayoutParams(layoutParams);

        cardContainer.addView(textListEmpty);
    }

    private void fetchReadedNotes() {
        UserDAO.getReadedNotesList(querySnapshot -> {
            if (querySnapshot == null || querySnapshot.isEmpty()) {
                textListEmpty.setVisibility(View.VISIBLE);
                return;
            } else if (textListEmpty.getVisibility() == View.VISIBLE) {
                textListEmpty.setVisibility(View.GONE);
            }
            for (DocumentSnapshot document : querySnapshot) {
                if (!notes.isEmpty() && notes.contains(document.getId())) {
                    return;
                }

                notes.add(document.getId());

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

                // TODO: review to improve security
                if (!username.equals("Anonimo")) {
                    card.setOnClickListener(v -> MoveActivity.addActivity(requireActivity(), AccountViewActivity.class, (intent) -> {
                        String uid = document.getString("userId");
                        intent.putExtra("UID", uid);
                    }));
                } else {
                    card.setOnClickListener(v -> Toast.makeText(binding.getRoot().getContext(), "Utente anonimo", Toast.LENGTH_SHORT).show());
                }

                ViewHolder viewHolder = new ViewHolder(card);
                viewHolder.title.setText(username);
                viewHolder.description.setText(description);
                viewHolder.city.setText(city);
                viewHolder.date.setText(formattedDate);

                cardContainer.addView(card);
            }
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