package com.peppe289.echotrail;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityPersonalInfoBinding;

import java.util.Objects;

public class PersonalInfoActivity extends AppCompatActivity {

    private ActivityPersonalInfoBinding binding;
    private com.google.android.material.textfield.TextInputEditText usernameEditText;
    private String currentUsername;
    private com.google.android.material.button.MaterialButton saveButton;
    private com.google.android.material.button.MaterialButton cancelButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initialization();
        loadDefaultValue();
        setUpToolBar();
        setUpButton();
    }

    private void setUpButton() {
        saveButton.setOnClickListener(v -> {
            String newUsername = Objects.requireNonNull(usernameEditText.getText()).toString();
            if (!newUsername.equals(currentUsername)) {
                UserController.setUsername(newUsername);
                finish();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void initialization() {
        usernameEditText = binding.usernameEditText;
        saveButton = binding.saveButton;
        cancelButton = binding.cancelButton;
    }

    private void loadDefaultValue() {
        UserController.getUsername(username -> {
            currentUsername = username;
            usernameEditText.setText(username);
        });
    }

    private void setUpToolBar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (!Objects.requireNonNull(usernameEditText.getText()).toString().equals(currentUsername)) {
                showCustomDialog(() -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void showCustomDialog(CallBackDialog callBackDialog) {
        // Inflate the custom layout
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom, null);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Restare nella schermata?")
                .setView(customView)
                .setPositiveButton("Ignora", (dialogInterface, i) -> callBackDialog.onPositiveClick())
                .setNegativeButton("Annulla", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private interface CallBackDialog {
        void onPositiveClick();
    }
}
