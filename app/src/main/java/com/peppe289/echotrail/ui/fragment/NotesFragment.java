package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.ui.activity.UserViewActivity;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.model.Note;
import com.peppe289.echotrail.ui.custom.CustomListView;
import com.peppe289.echotrail.utils.*;
import com.peppe289.echotrail.adapter.NoteCustomAdapter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {
    private final List<String> loadedNoteIds = new ArrayList<>();
    private FragmentNotesBinding viewBinding;
    private NoteCustomAdapter notesAdapter;
    private LoadingManager loadingIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inizializzazione del binding
        viewBinding = FragmentNotesBinding.inflate(inflater, container, false);

        initializeUI();
        fetchNotesFromDatabase();

        return viewBinding.getRoot();
    }

    private void initializeUI() {
        // Configurazione del caricamento e dell'adapter per la lista di note
        loadingIndicator = new LoadingManager(viewBinding.getRoot());
        notesAdapter = new NoteCustomAdapter(requireContext(), R.layout.card_item, new ArrayList<>(),
                id -> NavigationHelper.addActivity(requireActivity(), UserViewActivity.class,
                        intent -> intent.putExtra("UID", id)));

        CustomListView notesListView = viewBinding.notesList;
        notesListView.setAdapter(notesAdapter);

        // Mostra l'indicatore di caricamento all'avvio
        loadingIndicator.showLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void fetchNotesFromDatabase() {
        UserController.getReadNotesList(new ControllerCallback<QuerySnapshot, ErrorType>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot == null || querySnapshot.isEmpty()) {
                    handleEmptyNoteList();
                    return;
                }

                updateNoteList(querySnapshot);
            }

            @Override
            public void onError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
            }
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

                String isFor = null;
                try {
                    // check if is dedicated note.
                    isFor = document.getString("send_to");
                } catch (Exception ignore) {
                }

                if (description != null && city != null) {
                    Note noteCard = new Note(
                            username == null ? "Anonimo" : username,
                            description,
                            formattedDate,
                            city,
                            username == null ? null : document.getString("userId"),
                            documentId,
                            // check if have this attribute and if is dedicated to the user.
                            (isFor != null && isFor.compareTo(UserController.getUid()) == 0)
                    );
                    notesAdapter.add(noteCard);
                    loadedNoteIds.add(documentId);
                }
            }
        }

        // Rimuove gli elementi non più presenti nel `querySnapshot`
        Iterator<Note> iterator = notesAdapter.getItems().iterator();
        while (iterator.hasNext()) {
            Note item = iterator.next();
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
