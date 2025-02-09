package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.databinding.ActivityContainerBinding;

public class ContainerActivity extends AppCompatActivity {
    public static final String FRAGMENT_KEY = "fragment_key";
    public static final String ARGS_KEY = "args_key";

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.peppe289.echotrail.databinding.ActivityContainerBinding binding =
                ActivityContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if the fragment is already in the container.
        if (savedInstanceState == null) {
            String fragmentName = getIntent().getExtras().getString(FRAGMENT_KEY);
            try {
                fragment = (Fragment) Class.forName(fragmentName).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // If not, add it to the container using a FragmentManager and a transaction.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragment = null;
    }
}
