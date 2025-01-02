package com.peppe289.echotrail.utils;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.peppe289.echotrail.AccountFragment;
import com.peppe289.echotrail.MapFragment;
import com.peppe289.echotrail.NotesFragment;
import com.peppe289.echotrail.R;

/**
 * The {@code BottomBar} class is a utility for managing interactions with the bottom navigation bar
 * and facilitating seamless fragment navigation in an Android application.
 *
 * <p>This class provides methods for handling menu item selections, dynamically switching between
 * fragments, and maintaining references to key fragments used in the application. It centralizes
 * navigation logic to simplify the management of the bottom bar and its associated components.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Provides resource ID retrieval methods for bottom bar menu items.</li>
 *     <li>Handles fragment transactions to display appropriate content based on user interaction.</li>
 *     <li>Supports dynamic fragment visibility using {@link FragmentTransaction#show} and {@link FragmentTransaction#hide}.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * BottomBar.setActivity(fragmentActivity);
 * BottomBar.setFragments(mapFragment, accountFragment, notesFragment);
 * bottomNavigationView.setOnItemSelectedListener(BottomBar::onNavigationItemSelected);
 * }</pre>
 *
 * <p><strong>Prerequisites:</strong> The hosting activity must be a {@link FragmentActivity}, and
 * fragments must be initialized and passed to this utility using {@link #setFragments(MapFragment, AccountFragment, NotesFragment)}.</p>
 *
 * @see R
 * @see MenuItem
 * @see Fragment
 * @see FragmentManager
 * @see FragmentTransaction
 */
public class BottomBar {

    /**
     * Holds a reference to the {@link MapFragment} used in the application.
     * This fragment is displayed when the map menu item is selected.
     */
    private static MapFragment mapFragment = null;

    /**
     * Holds a reference to the {@link AccountFragment} used in the application.
     * This fragment is displayed when the settings menu item is selected.
     */
    private static AccountFragment accountFragment = null;

    /**
     * Holds a reference to the {@link NotesFragment} used in the application.
     * This fragment is displayed when the notes menu item is selected.
     */
    private static NotesFragment notesFragment = null;

    /**
     * Manages all fragment transactions for this utility. Must be initialized
     * by calling {@link #setActivity(FragmentActivity)}.
     */
    private static FragmentManager fragmentManager;

    /**
     * Retrieves the resource ID of the bottom navigation view.
     *
     * <p>The ID is defined as {@code R.id.bottomNavigationView} in the application's resources.</p>
     *
     * @return The resource ID for the bottom navigation view.
     */
    @SuppressWarnings("unused")
    public static int getBottomBarId() {
        return R.id.bottomNavigationView;
    }

    /**
     * Sets the fragments to be managed by the bottom navigation utility.
     *
     * <p>All fragments must be initialized and passed to this method before navigation is attempted.</p>
     *
     * @param mapFragment     The {@link MapFragment} instance for the map view.
     * @param accountFragment The {@link AccountFragment} instance for the account view.
     * @param notesFragment   The {@link NotesFragment} instance for the notes view.
     */
    public static void setFragments(MapFragment mapFragment, AccountFragment accountFragment, NotesFragment notesFragment) {
        BottomBar.mapFragment = mapFragment;
        BottomBar.accountFragment = accountFragment;
        BottomBar.notesFragment = notesFragment;
    }

    /**
     * Retrieves the resource ID of the map menu button.
     *
     * <p>The ID is defined as {@code R.id.mapMenu} in the application's resources.</p>
     *
     * @return The resource ID for the map menu button.
     */
    public static int getMapBtnId() {
        return R.id.mapMenu;
    }

    /**
     * Retrieves the resource ID of the notes menu button.
     *
     * <p>The ID is defined as {@code R.id.noteMenu} in the application's resources.</p>
     *
     * @return The resource ID for the notes menu button.
     */
    public static int getNotesBtnId() {
        return R.id.noteMenu;
    }

    /**
     * Retrieves the resource ID of the settings menu button.
     *
     * <p>The ID is defined as {@code R.id.settingsMenu} in the application's resources.</p>
     *
     * @return The resource ID for the settings menu button.
     */
    public static int getSettingsBtnId() {
        return R.id.settingsMenu;
    }

    /**
     * Handles navigation item selection and displays the appropriate fragment.
     *
     * <p>This method matches the selected menu item to a fragment and updates the
     * fragment container using a transaction. The menu options supported are:</p>
     * <ul>
     *     <li>{@link #getMapBtnId()}: Displays the {@link MapFragment}.</li>
     *     <li>{@link #getNotesBtnId()}: Displays the {@link NotesFragment}.</li>
     *     <li>{@link #getSettingsBtnId()}: Displays the {@link AccountFragment}.</li>
     * </ul>
     *
     * @param item The selected {@link MenuItem}.
     * @return {@code true} if the menu item was handled successfully, {@code false} otherwise.
     */
    public static boolean onNavigationItemSelected(MenuItem item) {
        int triggerBtn = item.getItemId();
        if (triggerBtn == getNotesBtnId()) {
            switchFragment(notesFragment);
        } else if (triggerBtn == getSettingsBtnId()) {
            switchFragment(accountFragment);
        } else if (triggerBtn == getMapBtnId()) {
            switchFragment(mapFragment);
        } else {
            return false;
        }

        return true;
    }

    /**
     * Switches the visible fragment in the container.
     *
     * <p>Hides all fragments except the one passed as a parameter and commits the transaction.</p>
     *
     * @param showFragment The fragment to be displayed.
     */
    private static void switchFragment(Fragment showFragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment.equals(showFragment)) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    /**
     * Sets the hosting {@link FragmentActivity} for fragment transactions.
     *
     * <p>This method initializes the {@link FragmentManager} required for navigation.
     * It must be called before attempting to load any fragments.</p>
     *
     * @param fragmentActivity The activity hosting the fragments.
     */
    public static void setActivity(@NonNull FragmentActivity fragmentActivity) {
        fragmentManager = fragmentActivity.getSupportFragmentManager();
    }
}

