package com.peppe289.echotrail;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.peppe289.echotrail.databinding.ActivityListNotesBinding;
import com.peppe289.echotrail.fragment.AvailableNotesFragment;
import com.peppe289.echotrail.fragment.UserListFragment;

import java.util.List;

public class NotesListActivity extends AppCompatActivity {
    private Fragment fragment;
    private boolean isAlreadyUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isAlreadyUsed = false;

        com.peppe289.echotrail.databinding.ActivityListNotesBinding binding = ActivityListNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<String> notes = getIntent().getStringArrayListExtra("notes");

        fragment = notes != null ? new AvailableNotesFragment() : new UserListFragment();

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpToolBar();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Load the notes from the activity.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (fragment instanceof AvailableNotesFragment && !isAlreadyUsed) {
            ((AvailableNotesFragment) fragment).loadFromActivity(getIntent().getStringArrayListExtra("notes"));
            isAlreadyUsed = !isAlreadyUsed;
        }
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
}
