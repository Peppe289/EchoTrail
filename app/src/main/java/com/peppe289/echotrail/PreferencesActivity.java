package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.peppe289.echotrail.controller.user.PreferencesHelper;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityPreferencesBinding;

public class PreferencesActivity extends AppCompatActivity {

    private ActivityPreferencesBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpToggle();
    }

    private void setUpToggle() {
        SwitchMaterial anonymousSwitch = binding.switchAnonymousToggle;
        // set default value in view
        UserController.getDefaultAnonymousPreference(binding.getRoot().getContext(), anonymousSwitch::setChecked);
        // add action listener
        anonymousSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> UserController.setDefaultAnonymousPreference(binding.getRoot().getContext(), isChecked));
    }
}
