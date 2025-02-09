package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;

import android.util.Log;
import android.widget.LinearLayout;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityPreferencesBinding;
import com.peppe289.echotrail.ui.fragment.LanguagesFragment;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.LoadingManager;
import com.peppe289.echotrail.utils.NavigationHelper;

public class PreferencesActivity extends AppCompatActivity {

    private ActivityPreferencesBinding binding;
    private LoadingManager loadingManager;

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

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        setUpToolBar();
        setUpToggle();
    }

    private void setUpToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setUpToggle() {
        SwitchMaterial anonymousSwitch = binding.switchAnonymousToggle;
        LinearLayout languages = binding.languagesLayout;
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

        languages.setOnClickListener(v -> {
            NavigationHelper.startActivityForFragment(PreferencesActivity.this, LanguagesFragment.class, null);
        });
    }
}
