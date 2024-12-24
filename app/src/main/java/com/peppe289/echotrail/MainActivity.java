package com.peppe289.echotrail;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityMainBinding;
import com.peppe289.echotrail.utils.MoveActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // if the user is already logged (from android sdk) skipp this first page.
        if (UserController.isLoggedIn()) {
            MoveActivity.rebaseActivity(MainActivity.this, DispatcherActivity.class);
        }

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.guestAccessBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DispatcherActivity.class);
            startActivity(intent);
        });

        binding.registrazioneBtn.setOnClickListener(view -> {
            MoveActivity.addActivity(MainActivity.this, RegistrazioneActivity.class);
        });
    }
}
