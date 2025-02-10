package com.peppe289.echotrail.ui.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.FragmentPreferencesBinding;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.NavigationHelper;

public class PreferencesFragment extends Fragment {

    private FragmentPreferencesBinding binding;
    private LoadingManager loadingManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPreferencesBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        setUpToggle();

        return view;
    }

    private void setUpToggle() {
        SwitchMaterial anonymousSwitch = binding.switchAnonymousToggle;
        LinearLayout languages = binding.languagesLayout;
        LinearLayout sessionManager = binding.sessionManager;
        // set default value in view
        UserController.getDefaultAnonymousPreference(
                new ControllerCallback<Boolean, ErrorType>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        anonymousSwitch.setChecked(result);
                        loadingManager.hideLoading();
                    }

                    @Override
                    public void onError(ErrorType error) {
                        anonymousSwitch.setChecked(false);
                    }
                });
        // add action listener
        anonymousSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                UserController.setDefaultAnonymousPreference(isChecked));

        languages.setOnClickListener(v ->
                NavigationHelper.startActivityForFragment(requireActivity(), LanguagesFragment.class, null));

        sessionManager.setOnClickListener(v ->
                NavigationHelper.startActivityForFragment(requireActivity(), FragmentSession.class, null));
    }
}
