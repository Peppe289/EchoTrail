package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.peppe289.echotrail.databinding.ActivityDispatcherBinding;
import com.peppe289.echotrail.utils.BottomBar;

public class DispatcherActivity extends AppCompatActivity {
    protected ActivityDispatcherBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dispatcher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dispatcher), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
          This will fix app crash for:
          - java.lang.IllegalStateException: FragmentManager has not been attached to a host.
         */
        BottomBar.setActivity(this);

        binding = ActivityDispatcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomBar.loadFragment(new MapFragment());

        // create a listener for the bottom navigation bar
        binding.bottomNavigationView.setOnItemSelectedListener(BottomBar::onNavigationItemSelected);
    }
}
