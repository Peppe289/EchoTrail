package com.peppe289.echotrail.utils;

import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.peppe289.echotrail.MapFragment;
import com.peppe289.echotrail.NotesFragment;
import com.peppe289.echotrail.R;
import com.peppe289.echotrail.AccountFragment;

/**
 * The {@code BottomBar} class provides utility methods for managing the bottom navigation bar
 * and handling user interactions with its menu items.
 *
 * <p>This class centralizes access to resource IDs for the bottom navigation bar components
 * and provides logic for fragment navigation based on user interaction. It includes static methods
 * for obtaining the resource IDs of various menu items and a navigation handler to load fragments
 * dynamically.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * int bottomBarId = BottomBar.getBottomBarId();
 * int mapButtonId = BottomBar.getMapBtnId();
 * BottomBar.onNavigationItemSelected(menuItem);
 * }</pre>
 *
 * <p><strong>Note:</strong> The {@code FragmentActivity} must be properly set for fragment transactions
 * to work as expected.</p>
 *
 * @see R
 * @see MenuItem
 * @see Fragment
 */
public class BottomBar {

    /**
     * Reference to the current {@link FragmentActivity}.
     * Used for performing fragment transactions. Must be set before calling {@code loadFragment}.
     */
    @Nullable
    private static FragmentActivity activity = null;

    /**
     * Returns the resource ID of the bottom navigation view.
     *
     * <p>This ID corresponds to the {@code R.id.bottomNavigationView} entry
     * defined in the application's resource files.</p>
     *
     * @return the resource ID of the bottom navigation view.
     */
    @SuppressWarnings("unused")
    static public int getBottomBarId() {
        return R.id.bottomNavigationView;
    }

    /**
     * Returns the resource ID of the map menu button.
     *
     * <p>This ID corresponds to the {@code R.id.mapMenu} entry
     * defined in the application's resource files.</p>
     *
     * @return the resource ID of the map menu button.
     */
    static public int getMapBtnId() {
        return R.id.mapMenu;
    }

    /**
     * Returns the resource ID of the notes menu button.
     *
     * <p>This ID corresponds to the {@code R.id.noteMenu} entry
     * defined in the application's resource files.</p>
     *
     * @return the resource ID of the notes menu button.
     */
    static public int getNotesBtnId() {
        return R.id.noteMenu;
    }

    /**
     * Returns the resource ID of the settings menu button.
     *
     * <p>This ID corresponds to the {@code R.id.settingsMenu} entry
     * defined in the application's resource files.</p>
     *
     * @return the resource ID of the settings menu button.
     */
    static public int getSettingsBtnId() {
        return R.id.settingsMenu;
    }

    /**
     * Handles navigation item selection events and loads the appropriate fragment.
     *
     * <p>This method identifies the menu item selected by the user, creates the corresponding
     * {@link Fragment}, and initiates a fragment transaction to display it in the designated container.</p>
     *
     * <p>Supported menu items:</p>
     * <ul>
     *     <li>{@code getNotesBtnId()}: Loads {@link NotesFragment}</li>
     *     <li>{@code getSettingsBtnId()}: Loads {@link AccountFragment}</li>
     *     <li>{@code getMapBtnId()}: Loads {@link MapFragment}</li>
     * </ul>
     *
     * @param item the {@link MenuItem} selected by the user.
     * @return {@code true} if a valid menu item was handled; {@code false} otherwise.
     */
    static public boolean onNavigationItemSelected(MenuItem item) {
        Fragment selectedFragment;
        int triggerBtn = item.getItemId();
        if (triggerBtn == getNotesBtnId()) {
            selectedFragment = new NotesFragment();
        } else if (triggerBtn == getSettingsBtnId()) {
            selectedFragment = new AccountFragment();
        } else if (triggerBtn == getMapBtnId()) {
            selectedFragment = new MapFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment);
        return true;
    }

    /**
     * Loads the specified {@link Fragment} into the designated container in the current activity.
     *
     * <p>This method performs a fragment transaction to replace the contents of the container
     * identified by {@code R.id.fragmentContainer} with the specified fragment.</p>
     *
     * <p><strong>Note:</strong> The {@code FragmentActivity} reference must be properly set before
     * invoking this method. If the activity is {@code null}, a new {@link FragmentActivity} is instantiated,
     * which may not align with the intended behavior.</p>
     *
     * @param fragment the {@link Fragment} to load into the container.
     * @throws IllegalStateException if {@code activity} is not set.
     */
    static public void loadFragment(Fragment fragment) {
        if (activity == null) {
            throw new IllegalStateException("FragmentActivity is not set. Call setActivity() before using loadFragment().");
        }

        activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

    /**
     * Sets the current {@link FragmentActivity} for fragment transactions.
     *
     * <p>This method must be called before invoking {@link #loadFragment(Fragment)} to ensure
     * the activity context is available for performing fragment transactions.</p>
     *
     * @param fragmentActivity the current {@link FragmentActivity}.
     */
    static public void setActivity(@Nullable FragmentActivity fragmentActivity) {
        activity = fragmentActivity;
    }
}
