package com.peppe289.echotrail.fragment;

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
import com.peppe289.echotrail.UserViewActivity;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.databinding.FragmentNotesBinding;
import com.peppe289.echotrail.model.NoteItem;
import com.peppe289.echotrail.utils.CardItemAdapter;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.MoveActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvailableNotesFragment extends Fragment {

    private CardItemAdapter cardItemAdapter;
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
        cardItemAdapter = new CardItemAdapter(requireContext(), R.layout.card_item, new ArrayList<>(),
                id -> MoveActivity.addActivity(requireActivity(), UserViewActivity.class,
                        intent -> intent.putExtra("UID", id)));

        listView.setAdapter(cardItemAdapter);

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

        NotesController.getNotes(this.noteIDs, new UserDAO.NotesListCallback() {
            @Override
            public void onComplete(QuerySnapshot notes) {
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
                        NoteItem noteItem = new NoteItem(
                                username == null ? "Anonimo" : username,
                                description,
                                formattedDate,
                                city,
                                username == null ? null : document.getString("userId"),
                                document.getId(),
                                // check if have this attribute and if is dedicated to the user.
                                (isFor != null && isFor.compareTo(UserController.getUid()) == 0)
                        );
                        cardItemAdapter.add(noteItem);
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
