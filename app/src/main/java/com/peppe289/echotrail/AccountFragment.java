package com.peppe289.echotrail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textview.MaterialTextView;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentAccountBinding;
import com.peppe289.echotrail.utils.MoveActivity;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private MaterialTextView username;
    private MaterialTextView email;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        username = binding.usernameTextView;
        email = binding.emailTextView;

        // TODO: implements the button function (should be open personal information for editing)
        binding.personalData.setOnClickListener(view -> Log.i("AccountFragment", "Edit profile button clicked"));

        // TODO: implements the button function (should be open notify settings)
        binding.notify.setOnClickListener(view -> Log.i("AccountFragment", "Notify settings button clicked"));

        binding.logoutBtn.setOnClickListener(view -> {
            UserController.logout(requireContext());
            MoveActivity.rebaseActivity(getActivity(), MainActivity.class, null);
            Log.i("AccountFragment", "User logged out");
        });

        AppBarLayout appBarLayout = binding.appBarLayout;

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            float percentage = Math.abs(verticalOffset) / (float) appBarLayout1.getTotalScrollRange();
            binding.userIcon.setScaleX(1 - percentage);
            binding.userIcon.setScaleY(1 - percentage);
            binding.userIcon.setAlpha(1 - percentage);
        });

        binding.mynotes.setOnClickListener(view -> MoveActivity.addActivity(requireActivity(), MyNotesActivity.class, null));

        // load user headers (name and email) from cache if possible.
        UserController.getUserHeadersFromPreferences(requireContext(), headers -> {
            username.setText(headers.get("username"));
            email.setText(headers.get("email"));
        });

        return rootView;
    }
}