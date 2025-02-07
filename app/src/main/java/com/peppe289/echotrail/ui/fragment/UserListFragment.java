package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.model.Note;
import com.peppe289.echotrail.adapter.NoteCustomAdapter;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserListFragment extends Fragment {
    private FragmentNotesBinding binding;

    private LoadingManager loadingManager;

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
        NoteCustomAdapter noteCustomAdapter = new NoteCustomAdapter(requireContext(), R.layout.card_item, new ArrayList<>(), null);

        listView.setAdapter(noteCustomAdapter);
        loadingManager.showLoading();

        UserController.getUserNotesList(new ControllerCallback<QuerySnapshot, ErrorType>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot == null || querySnapshot.isEmpty()) {
                    TextView textView = binding.textListEmpty;
                    textView.setVisibility(View.VISIBLE);
                } else {
                    for (DocumentSnapshot document : querySnapshot) {
                        Log.i("UserListFragment", "Document: " + document);
                        String city = document.getString("city");
                        String description = document.getString("content");
                        Timestamp timestamp = (Timestamp) document.get("timestamp");
                        String formattedDate = formatDate(timestamp);

                        if (description != null && city != null) {
                            Note note = new Note("La tua nota", description, formattedDate, city, null, document.getId(), false);
                            noteCustomAdapter.add(note);
                        }
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

        return view;
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
