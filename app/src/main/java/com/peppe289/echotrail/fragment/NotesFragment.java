package com.peppe289.echotrail.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.AccountViewActivity;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.model.CardItem;
import com.peppe289.echotrail.utils.CardItemAdapter;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.MoveActivity;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

    private final ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    private final List<String> loadedNoteIds = new ArrayList<>();
    private FragmentNotesBinding viewBinding;
    private ScheduledFuture<?> periodicFetchTask;
    private CardItemAdapter notesAdapter;
    private LoadingManager loadingIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inizializzazione del binding
        viewBinding = FragmentNotesBinding.inflate(inflater, container, false);

        // Configurazione dell'interfaccia utente
        initializeUI();
        schedulePeriodicNoteFetch();

        return viewBinding.getRoot();
    }

    private void initializeUI() {
        // Configurazione del caricamento e dell'adapter per la lista di note
        loadingIndicator = new LoadingManager(viewBinding.getRoot());
        notesAdapter = new CardItemAdapter(requireContext(), R.layout.card_item, new ArrayList<>(),
                id -> MoveActivity.addActivity(requireActivity(), AccountViewActivity.class,
                        intent -> intent.putExtra("UID", id)));

        // Imposta l'adapter sulla ListView
        viewBinding.notesList.setAdapter(notesAdapter);

        // Mostra l'indicatore di caricamento all'avvio
        loadingIndicator.showLoading();

        // Aggiunge un listener per la selezione di un elemento della lista
        viewBinding.notesList.setOnItemClickListener((parent, view, position, id) -> {
            CardItem selectedNote = notesAdapter.getItem(position);

            if (selectedNote != null && selectedNote.getUserId() != null) {
                MoveActivity.addActivity(requireActivity(), AccountViewActivity.class, (intent) -> {
                    String uid = selectedNote.getUserId();
                    intent.putExtra("UID", uid);
                });
            }
        });
    }

    private void schedulePeriodicNoteFetch() {
        // Pianifica il recupero dei dati ogni 5 secondi
        periodicFetchTask = backgroundExecutor.scheduleWithFixedDelay(this::fetchNotesFromDatabase, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Interrompe il task periodico e chiude l'executor
        if (periodicFetchTask != null && !periodicFetchTask.isCancelled()) {
            periodicFetchTask.cancel(true);
        }
        backgroundExecutor.shutdown();
    }

    private void fetchNotesFromDatabase() {
        // Recupera le note dal database utilizzando UserDAO
        UserDAO.getReadedNotesList(querySnapshot -> {
            if (querySnapshot == null || querySnapshot.isEmpty()) {
                handleEmptyNoteList();
                return;
            }

            updateNoteList(querySnapshot);
        });
    }

    private void handleEmptyNoteList() {
        // Mostra un messaggio se la lista è vuota
        viewBinding.textListEmpty.setVisibility(View.VISIBLE);
        loadingIndicator.hideLoading();
    }

    private void updateNoteList(QuerySnapshot querySnapshot) {
        if (querySnapshot == null || querySnapshot.isEmpty()) {
            // Mostra il messaggio di lista vuota se non ci sono dati
            viewBinding.textListEmpty.setVisibility(View.VISIBLE);
            notesAdapter.clear(); // Pulisci la lista se non ci sono più elementi
            loadedNoteIds.clear();
            notesAdapter.notifyDataSetChanged();
            loadingIndicator.hideLoading();
            viewBinding.notesList.setVisibility(View.GONE);
            return;
        }

        // Nasconde il messaggio di lista vuota poiché ci sono dati
        viewBinding.textListEmpty.setVisibility(View.GONE);
        viewBinding.notesList.setVisibility(View.VISIBLE);

        // Mappa temporanea per verificare quali elementi mantenere
        Set<String> currentNoteIds = new HashSet<>();

        // Aggiunge o aggiorna gli elementi
        for (DocumentSnapshot document : querySnapshot) {
            String documentId = document.getId();
            currentNoteIds.add(documentId);

            if (!loadedNoteIds.contains(documentId)) {
                // Nuovo elemento trovato, aggiungilo all'adapter
                String city = document.getString("city");
                String description = document.getString("content");
                Timestamp timestamp = (Timestamp) document.get("timestamp");
                String formattedDate = formatTimestamp(timestamp);
                String username = document.getString("username");

                if (description != null && city != null) {
                    CardItem noteCard = new CardItem(
                            username == null ? "Anonimo" : username,
                            description,
                            formattedDate,
                            city,
                            username == null ? null : document.getString("userId"),
                            documentId
                    );
                    notesAdapter.add(noteCard);
                    loadedNoteIds.add(documentId);
                }
            }
        }

        // Rimuove gli elementi non più presenti nel `querySnapshot`
        Iterator<CardItem> iterator = notesAdapter.getItems().iterator();
        while (iterator.hasNext()) {
            CardItem item = iterator.next();
            if (!currentNoteIds.contains(item.getId())) {
                iterator.remove();
                loadedNoteIds.remove(item.getId());
            }
        }

        // Notifica l'adapter per aggiornare la lista visivamente
        notesAdapter.notifyDataSetChanged();
        loadingIndicator.hideLoading();
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormatter.format(date);
    }
}
