package com.peppe289.echotrail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.utils.MoveActivity;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // TODO: implements the button function (should be open personal notes list)
        binding.mynotes.setOnClickListener(view -> Log.i("AccountFragment", "My notes button clicked"));

        binding.logoutBtn.setOnClickListener(view -> {
            UserController.logout();
            MoveActivity.rebaseActivity(getActivity(), MainActivity.class);
            Log.i("AccountFragment", "User logged out");
        });

        return rootView;
    }
}