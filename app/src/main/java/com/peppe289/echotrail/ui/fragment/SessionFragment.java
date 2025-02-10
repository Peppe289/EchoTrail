package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.adapter.SessionsAdapter;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentSessionBinding;
import com.peppe289.echotrail.model.Session;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.List;

public class SessionFragment extends Fragment {
    private ListView listView;
    private FragmentSessionBinding binding;
    private SessionsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSessionBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        listView = v.findViewById(R.id.session_list);
        loadSession();
        initListener();

        return v;
    }

    public void initListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Session session = adapter.getItem(position);
            if (session != null) {
                showConfirmationDialog(session);
            }
        });
    }

    private void showConfirmationDialog(Session session) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.confirm_elimination))
                .setMessage(getString(R.string.confirm_elimination_desc))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> removeSession(session))
                .setNegativeButton(getString(R.string.abort), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeSession(Session session) {
        UserController.removeSession(session.getId(), new ControllerCallback<Void, ErrorType>() {
            @Override
            public void onSuccess(Void aVoid) {
                adapter.remove(session);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Sessione rimossa con successo!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loadSession() {
        UserController.getAllSessions(new ControllerCallback<List<Session>, ErrorType>() {
            @Override
            public void onSuccess(List<Session> sessions) {
                adapter = new SessionsAdapter(requireContext(), R.layout.session_item, sessions);
                listView.setAdapter(adapter);
            }

            @Override
            public void onError(ErrorType errorType) {
                Toast.makeText(requireContext(), errorType.getMessage(requireContext()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
