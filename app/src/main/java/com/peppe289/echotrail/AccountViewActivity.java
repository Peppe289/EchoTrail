package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityAccountViewBinding;

public class AccountViewActivity extends AppCompatActivity {

    private ActivityAccountViewBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String UID = getIntent().getStringExtra("UID");


        UserController.getUserInfoByUID(UID, userInfo -> {
            binding.usernameTextView.setText(userInfo.get("username").toString());
            binding.notesRead.setText(userInfo.get("readedNotes").toString());
            binding.notesPublished.setText(userInfo.get("notes").toString());
        });
    }
}
