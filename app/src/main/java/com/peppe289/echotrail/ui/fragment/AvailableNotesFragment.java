package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.ui.activity.UserViewActivity;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.model.Note;
import com.peppe289.echotrail.adapter.NoteCustomAdapter;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.NavigationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvailableNotesFragment extends Fragment {

    private NoteCustomAdapter noteCustomAdapter;
    private FragmentNotesBinding binding;
    private LoadingManager loadingManager;
    private List<String> noteIDs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        binding = FragmentNotesBinding.bind(view);
        ListView listView = binding.notesList;
        loadingManager = new LoadingManager(binding.getRoot());
        noteCustomAdapter = new NoteCustomAdapter(requireContext(), R.layout.card_item, new ArrayList<>(),
                id -> NavigationHelper.addActivity(requireActivity(), UserViewActivity.class,
                        intent -> intent.putExtra("UID", id)));

        listView.setAdapter(noteCustomAdapter);

        if (getArguments() != null) {
            List<String> noteIDs = getArguments().getStringArrayList("notes");
            loadFromActivity(noteIDs);
        }

        return view;
    }

    public void loadFromActivity(List<String> noteIDs) {
        if (this.noteIDs == null) {
            this.noteIDs = new ArrayList<>();
            this.noteIDs.addAll(noteIDs);
        } else {
            /*
              When we got back from the account details activity, this
              will run again, but this can cause duplicates.
             */
            for (String noteID : noteIDs) {
                if (!this.noteIDs.contains(noteID)) {
                    this.noteIDs.add(noteID);
                }
            }
        }

        NotesController.getNotes(this.noteIDs, new ControllerCallback<QuerySnapshot, ErrorType>() {
            @Override
            public void onSuccess(QuerySnapshot notes) {
                if (notes == null || notes.isEmpty()) {
                    TextView textView = binding.textListEmpty;
                    textView.setVisibility(View.VISIBLE);
                    loadingManager.hideLoading();
                    return;
                }
                for (DocumentSnapshot document : notes) {
                    String city = document.getString("city");
                    String description = document.getString("content");
                    Timestamp timestamp = (Timestamp) document.get("timestamp");
                    String formattedDate = formatDate(timestamp);
                    String username = document.getString("username");

                    String isFor = null;
                    try {
                        // check if is dedicated note.
                        isFor = document.getString("send_to");
                    } catch (Exception ignore) {
                    }

                    if (description != null && city != null) {
                        Note note = new Note(
                                username == null ? "Anonimo" : username,
                                description,
                                formattedDate,
                                city,
                                username == null ? null : document.getString("userId"),
                                document.getId(),
                                // check if have this attribute and if is dedicated to the user.
                                (isFor != null && isFor.compareTo(UserController.getUid()) == 0)
                        );
                        noteCustomAdapter.add(note);
                    }
                }

                loadingManager.hideLoading();
            }

            @Override
            public void onError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
                loadingManager.hideLoading();
            }
        });
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }
}
