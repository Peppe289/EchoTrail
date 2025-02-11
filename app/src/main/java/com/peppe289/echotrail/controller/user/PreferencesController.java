package com.peppe289.echotrail.controller.user;

import android.content.Context;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.HashMap;
import java.util.Locale;

/**
 * The {@code PreferencesHelper} class provides utility methods for managing user preferences
 * such as username and email. It leverages shared preferences for local storage and Firebase
 * as a fallback for retrieving user data.
 * <p>
 * Main responsibilities include:
 * <ul>
 *     <li>Loading user data (username and email) from Firebase if not available locally.</li>
 *     <li>Saving retrieved user data into shared preferences for future use.</li>
 *     <li>Retrieving user data from shared preferences when available.</li>
 *     <li>Clearing stored user data from shared preferences upon request.</li>
 * </ul>
 */
public class PreferencesController {
    private static SharedPreferences userPreferences;
    private static SharedPreferences settingsPreferences;

    public static void init(Context context) {
        userPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        settingsPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    /**
     * Retrieves the current language settings from shared preferences.
     *
     * @return string representing the current language settings like "it" or "en"
     */
    public static String getLanguages() {
        return settingsPreferences
                .getString("languages", Locale.getDefault().getLanguage());
    }

    /**
     * Sets the languages in shared preferences.
     *
     * @param languages languages to be stored like "it" or "en"
     */
    public static void setLanguages(String languages) {
        settingsPreferences.edit().putString("languages", languages).apply();
    }

    /**
     * Updates the username in shared preferences.
     *
     * @param name    The new username to be stored.
     */
    public static void updateName(String name) {
        userPreferences
                .edit()
                .putString("username", name)
                .apply();
    }

    /**
     * Updates the email in shared preferences.
     *
     * @param email   The new email to be stored.
     */
    public static void updateEmail(String email) {
        userPreferences
                .edit()
                .putString("email", email)
                .apply();
    }

    /**
     * Retrieves the email from shared preferences, if available.
     *
     * @return The stored email as a string, or {@code null} if no email is stored.
     */
    public static @Nullable String retrieveEmail() {
        return userPreferences
                .getString("email", null);
    }

    /**
     * Retrieves the username from shared preferences, if available.
     *
     * @return The stored username as a string, or {@code null} if no username is stored.
     */
    public static @Nullable String retrieveName() {
        return userPreferences
                .getString("username", null);
    }

    /**
     * Verifies if user preferences (username and email) are available in shared preferences.
     * <p>
     * If preferences are available, it invokes the callback with the stored data. If not, it
     * attempts to fetch the data from Firebase, saves it locally, and then invokes the callback.
     *
     * @param callback A callback to be invoked once the data is available.
     *                 The callback receives a map containing the keys "username" and "email".
     */
    public static void loadFromPreferences(ControllerCallback<HashMap<String, String>, ErrorType> callback) {
        @Nullable String username = retrieveName();
        @Nullable String email = retrieveEmail();

        if (callback != null) {
            // Invoke the callback with the data from shared preferences.
            callback.onSuccess(new HashMap<>(2) {{
                put("username", username);
                put("email", email);
            }});
        }
    }

    /**
     * Clears the user headers (username and email) stored in shared preferences.
     * <p>
     * This method removes any locally stored user data to ensure that no stale information
     * remains in the shared preferences.
     */
    public static void clearUserHeaders() {
        userPreferences
                .edit()
                .remove("username")
                .remove("email")
                .apply();
    }

    public static void setAnonymousPreferences(boolean isChecked) {
        userPreferences
                .edit()
                .putBoolean("anonymousByDefault", isChecked)
                .apply();
    }

    public static boolean getAnonymousPreferences() {
        return userPreferences
                .getBoolean("anonymousByDefault", false);
    }

    public static void clearAnonymousPreferences() {
        userPreferences
                .edit()
                .remove("anonymousByDefault")
                .apply();
    }
}
