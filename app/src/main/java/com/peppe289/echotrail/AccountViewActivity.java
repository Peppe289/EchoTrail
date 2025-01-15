package com.peppe289.echotrail;

import android.os.Bundle;

import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityAccountViewBinding;
import com.peppe289.echotrail.utils.LoadingManager;

public class AccountViewActivity extends AppCompatActivity {

    private ActivityAccountViewBinding binding;
    private LoadingManager loadingManager;

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

        loadingManager = new LoadingManager(binding.getRoot());
        loadingManager.showLoading();

        String UID = getIntent().getStringExtra("UID");

        UserController.getUserInfoByUID(UID, userInfo -> {
            if (userInfo != null) {
                setTextViewIfNotNull(binding.usernameTextView, userInfo.get("username"));
                setTextViewIfNotNull(binding.notesRead, userInfo.get("readedNotes"));
                setTextViewIfNotNull(binding.notesPublished, userInfo.get("notes"));
                loadingManager.hideLoading();
            }
        });
    }

    private void setTextViewIfNotNull(TextView textView, Object value) {
        if (value != null) {
            textView.setText(value.toString());
        }
    }
}
