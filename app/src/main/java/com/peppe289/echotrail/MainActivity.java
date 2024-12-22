package com.peppe289.echotrail;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.peppe289.echotrail.databinding.ActivityMainBinding;
import com.peppe289.echotrail.model.BottomBar;

public class MainActivity extends AppCompatActivity {
    protected ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
          This will fix app crash for:
          - java.lang.IllegalStateException: FragmentManager has not been attached to a host.
         */
        BottomBar.setActivity(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomBar.loadFragment(new MapFragment());

        // create a listener for the bottom navigation bar
        binding.bottomNavigationView.setOnItemSelectedListener(BottomBar::onNavigationItemSelected);
    }
}