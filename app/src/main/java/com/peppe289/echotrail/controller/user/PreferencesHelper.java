package com.peppe289.echotrail.controller.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.HashMap;

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
public class PreferencesHelper {

    /**
     * Loads the user headers (username and email) from Firebase and stores them in shared preferences.
     * <p>
     * The method performs the following steps:
     * <ol>
     *     <li>Asynchronously retrieves the username from Firebase.</li>
     *     <li>Retrieves the email from Firebase after the username is fetched.</li>
     *     <li>Saves both username and email in shared preferences.</li>
     *     <li>Invokes the provided callback with the retrieved data as a map.</li>
     * </ol>
     *
     * @param context  The application context used to access shared preferences.
     * @param callback A callback to be invoked once the data is retrieved and stored.
     *                 The callback receives a map containing the keys "username" and "email".
     */
    private static void loadUserHeaders(Context context, UserController.UserHeadersCallback callback) {
        UserController.getUsername(name -> UserController.getEmail(emailStr -> {
            updateName(context, name);
            updateEmail(context, emailStr);

            if (callback != null) {
                // Provide the retrieved data to the callback.
                callback.onComplete(new HashMap<>(2) {{
                    put("username", name);
                    put("email", emailStr);
                }});
            }
        }));
    }

    /**
     * Updates the username in shared preferences.
     *
     * @param context The application context used to access shared preferences.
     * @param name    The new username to be stored.
     */
    public static void updateName(Context context, String name) {
        context.getSharedPreferences("user", MODE_PRIVATE)
                .edit()
                .putString("username", name)
                .apply();
    }

    /**
     * Updates the email in shared preferences.
     *
     * @param context The application context used to access shared preferences.
     * @param email   The new email to be stored.
     */
    public static void updateEmail(Context context, String email) {
        context.getSharedPreferences("user", MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .apply();
    }

    /**
     * Retrieves the email from shared preferences, if available.
     *
     * @param context The application context used to access shared preferences.
     * @return The stored email as a string, or {@code null} if no email is stored.
     */
    public static @Nullable String retrieveEmail(Context context) {
        return context.getSharedPreferences("user", MODE_PRIVATE)
                .getString("email", null);
    }

    /**
     * Retrieves the username from shared preferences, if available.
     *
     * @param context The application context used to access shared preferences.
     * @return The stored username as a string, or {@code null} if no username is stored.
     */
    public static @Nullable String retrieveName(Context context) {
        return context.getSharedPreferences("user", MODE_PRIVATE)
                .getString("username", null);
    }

    /**
     * Verifies if user preferences (username and email) are available in shared preferences.
     * <p>
     * If preferences are available, it invokes the callback with the stored data. If not, it
     * attempts to fetch the data from Firebase, saves it locally, and then invokes the callback.
     *
     * @param context  The application context used to access shared preferences.
     * @param callback A callback to be invoked once the data is available.
     *                 The callback receives a map containing the keys "username" and "email".
     */
    public static void checkOnPreferences(Context context, UserController.UserHeadersCallback callback) {
        @Nullable String username = retrieveName(context);
        @Nullable String email = retrieveEmail(context);

        if (username == null || email == null) {
            // Load data from Firebase if not present in shared preferences.
            loadUserHeaders(context, callback);
        } else if (callback != null) {
            // Invoke the callback with the data from shared preferences.
            callback.onComplete(new HashMap<>(2) {{
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
     *
     * @param context The application context used to access shared preferences.
     */
    public static void clearUserHeaders(Context context) {
        context.getSharedPreferences("user", MODE_PRIVATE)
                .edit()
                .remove("username")
                .remove("email")
                .apply();
    }
}
