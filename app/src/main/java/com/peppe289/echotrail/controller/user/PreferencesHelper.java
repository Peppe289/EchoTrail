package com.peppe289.echotrail.controller.user;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.HashMap;

/**
 * The {@code PreferencesHelper} class provides utility methods to load and manage user preferences
 * such as username and email, using shared preferences or Firebase as a fallback mechanism.
 * This class handles loading the user data from the local shared preferences and, if unavailable,
 * retrieves the data from Firebase and saves it to the shared preferences for future use.
 * Additionally, it provides a method to clear the stored user data from shared preferences.
 */
public class PreferencesHelper {

    /**
     * Loads the user headers (username and email) from Firebase and stores them in shared preferences.
     * This method retrieves the username and email asynchronously from Firebase, stores the values
     * in shared preferences, and then invokes the provided callback with the retrieved data.
     *
     * @param context  The application context used to access shared preferences.
     * @param callback A callback that will be invoked once the user headers (username and email)
     *                 are retrieved and stored. The callback will receive a map containing the
     *                 "username" and "email" as keys.
     */
    private static void loadUserHeaders(Context context, UserController.UserHeadersCallback callback) {
        // Try first to retrieve user name from Firebase, then try to retrieve user email from Firebase.
        UserController.getUsername(name -> UserController.getEmail(emailStr -> {
            // Save user name and email to shared preferences.
            context.getSharedPreferences("user", MODE_PRIVATE)
                    .edit()
                    .putString("username", name)
                    .apply();

            context.getSharedPreferences("user", MODE_PRIVATE)
                    .edit()
                    .putString("email", emailStr)
                    .apply();

            // Run callback function with the retrieved data.
            callback.onComplete(new HashMap<>(2) {{
                put("username", name);
                put("email", emailStr);
            }});
        }));
    }

    /**
     * Checks if the user preferences (username and email) are available in shared preferences.
     * If the preferences are available, it invokes the callback with the data. If not, it attempts
     * to load the data from Firebase and then invokes the callback with the retrieved information.
     *
     * @param context  The application context used to access shared preferences.
     * @param callback A callback that will be invoked once the user headers (username and email)
     *                 are available. The callback will receive a map containing the "username"
     *                 and "email" as keys.
     */
    public static void checkOnPreferences(Context context, UserController.UserHeadersCallback callback) {
        // Retrieve username and email from shared preferences if available.
        @Nullable String username = context.getSharedPreferences("user", MODE_PRIVATE)
                .getString("username", null);
        @Nullable String email = context.getSharedPreferences("user", MODE_PRIVATE)
                .getString("email", null);

        // If the cached values are empty, retrieve the information from Firebase.
        if (username == null || email == null) {
            loadUserHeaders(context, callback);
        } else {
            // Invoke the callback with the data retrieved from shared preferences.
            callback.onComplete(new HashMap<>(2) {{
                put("username", username);
                put("email", email);
            }});
        }
    }

    /**
     * Clears the user headers (username and email) from shared preferences.
     * This method removes the stored username and email from the shared preferences.
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
