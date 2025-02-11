package com.peppe289.echotrail.ui.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.databinding.ActivityDispatcherBinding;
import com.peppe289.echotrail.ui.fragment.AccountFragment;
import com.peppe289.echotrail.ui.fragment.MapFragment;
import com.peppe289.echotrail.ui.fragment.NotesFragment;
import com.peppe289.echotrail.ui.utils.BottomBar;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code DispatcherActivity} class serves as the main activity for managing the application's
 * fragment navigation and user interface interactions. It initializes the bottom navigation bar,
 * configures fragment transactions, and applies system UI adjustments for a seamless user experience.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Initializes and manages three primary fragments: {@link MapFragment}, {@link AccountFragment}, and {@link NotesFragment}.</li>
 *     <li>Configures the {@link BottomBar} utility for handling bottom navigation events.</li>
 *     <li>Applies edge-to-edge rendering for modern Android UI.</li>
 *     <li>Synchronizes user headers with local preferences using {@link UserController#updateUserHeadersToPreferences}.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <p>This activity is typically defined as the entry point in the application's manifest:</p>
 * <pre>{@code
 * <activity android:name=".DispatcherActivity">
 *     <intent-filter>
 *         <action android:name="android.intent.action.MAIN" />
 *         <category android:name="android.intent.category.LAUNCHER" />
 *     </intent-filter>
 * </activity>
 * }</pre>
 *
 * <h2>Dependencies:</h2>
 * <ul>
 *     <li>{@link BottomBar}: Utility class for managing bottom navigation bar interactions.</li>
 *     <li>{@link UserController}: Class responsible for managing user data and preferences.</li>
 *     <li>{@link EdgeToEdge}: Utility for enabling edge-to-edge system UI.</li>
 * </ul>
 *
 * @see AppCompatActivity
 * @see MapFragment
 * @see AccountFragment
 * @see NotesFragment
 * @see BottomBar
 */
public class DispatcherActivity extends AppCompatActivity {

    /**
     * Binding object for accessing and manipulating views in {@code activity_dispatcher.xml}.
     */
    protected ActivityDispatcherBinding binding;

    /**
     * Manages fragment transactions for dynamically displaying content in the activity.
     */
    FragmentManager fragmentManager = getSupportFragmentManager();

    /**
     * Called when the activity is created.
     *
     * <p>This method initializes the UI components, configures fragment transactions, sets up
     * the bottom navigation listener, and applies system UI adjustments for edge-to-edge content rendering.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down,
     *                           this contains the data it most recently supplied. Otherwise, it is {@code null}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge rendering for the activity.
        EdgeToEdge.enable(this);

        // Set the content view layout.
        setContentView(R.layout.activity_dispatcher);

        // Apply system bar insets to the root view to support edge-to-edge layout.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dispatcher), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // custom keyboard insets
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            // keep the padding bottom if the keyboard is shown
            if (!imeInsets.equals(Insets.NONE)) {
                // ignore keyboard insets
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom + imeInsets.bottom);
            } else {
                // if the keyboard is hidden, apply system bar insets
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }

            return insets;
        });

        UserController.init();

        // Initialize fragments for navigation.
        MapFragment mapFragment = new MapFragment();
        AccountFragment accountFragment = new AccountFragment();
        NotesFragment notesFragment = new NotesFragment();

        // Configure fragment transactions to manage the initial state.
        fragmentManager.beginTransaction().add(R.id.fragment_container, mapFragment, "MapFragment")
                .add(R.id.fragment_container, accountFragment, "AccountFragment")
                .add(R.id.fragment_container, notesFragment, "NotesFragment")
                .hide(accountFragment) // Hide the account fragment by default.
                .hide(notesFragment)   // Hide the notes fragment by default.
                .commit();

        /*
         * Fixes potential crash caused by:
         * java.lang.IllegalStateException: FragmentManager has not been attached to a host.
         */
        BottomBar.setActivity(this);
        BottomBar.setFragments(mapFragment, accountFragment, notesFragment);

        // Inflate the binding for accessing the root view and child components.
        binding = ActivityDispatcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set default button selection for the bottom navigation bar.
        binding.bottomNavigationView.setSelectedItemId(BottomBar.getMapBtnId());
        // Set up a listener for the bottom navigation bar to handle menu item selections.
        binding.bottomNavigationView.setOnItemSelectedListener(BottomBar::onNavigationItemSelected);

        // Restore the selected item index from the saved instance state.
        if (savedInstanceState != null) {
            int selectedItemId = savedInstanceState.getInt("selectedItemId");
            // Using post to avoid IllegalStateException: Navigation is not allowed after onSaveInstanceState.
            // Wait in async state for the view to be created before selecting the item.
            binding.bottomNavigationView.post(() -> binding.bottomNavigationView.setSelectedItemId(selectedItemId));
        }
    }

    /**
     * View the android:configChanges="uiMode" attribute in the AndroidManifest.xml file this method
     * is called when change theme configuration. This should solve the problem of resetting the index
     * selected in the bottom bar (for example if I change theme when I'm in the notes page the main
     * page of the map is then selected).
     *
     * @param outState exit state before destroy.
     */
    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int selectedItemId = binding.bottomNavigationView.getSelectedItemId();
        outState.putInt("selectedItemId", selectedItemId);
    }
}
