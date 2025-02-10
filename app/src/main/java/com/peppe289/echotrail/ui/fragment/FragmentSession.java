package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.adapter.SessionsAdapter;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentSessionBinding;
import com.peppe289.echotrail.model.Session;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.List;

public class FragmentSession extends Fragment {
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
                // TODO: implement method to remove session
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
