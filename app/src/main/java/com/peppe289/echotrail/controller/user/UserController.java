package com.peppe289.echotrail.controller.user;

import android.content.Context;

import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.HashMap;
import java.util.Objects;

/**
 * The {@code UserController} class acts as an intermediary between the application logic
 * and the {@link UserDAO}, providing high-level user-related operations such as login,
 * registration, session management, and data retrieval.
 * <p>
 * This class abstracts the complexities of interacting with Firebase Authentication,
 * Shared Preferences, and other backend services, ensuring consistency and maintaining
 * the application's state integrity.
 * </p>
 * <p>
 * Core functionalities include:
 * <ul>
 *     <li>Login and registration for users.</li>
 *     <li>Checking if a user is logged in.</li>
 *     <li>Session handling, including logout and user data management.</li>
 *     <li>Fetching and updating user information like username and email.</li>
 *     <li>Managing shared preferences for user headers.</li>
 * </ul>
 * </p>
 */
public class UserController {

    /**
     * Logs in a user using their email and password.
     * <p>
     * This method ensures no user is already logged in before initiating the login process.
     * On successful login, it invokes the provided {@link UserDAO.SignInCallback}.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback A callback for handling the login result.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the provided callback is {@code null}.
     */
    public static void login(String email, String password, UserDAO.SignInCallback callback) {
        validateCallback(callback);

        if (!isLoggedIn()) {
            UserDAO.signIn(email, password, callback);
        } else {
            throw new UserStateException("User is already signed in.");
        }
    }

    /**
     * Registers a new user with the provided credentials and username.
     * <p>
     * Ensures no user is logged in before attempting to register a new account.
     * On successful registration, invokes the provided {@link UserDAO.SignUpCallback}.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's desired username.
     * @param callback A callback for handling the registration result.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the provided callback is {@code null}.
     */
    public static void register(String email, String password, String username, UserDAO.SignUpCallback callback) {
        validateCallback(callback);

        if (!isLoggedIn()) {
            UserDAO.signUp(email, password, username, callback);
        } else {
            throw new UserStateException("User is already signed in.");
        }
    }

    /**
     * Checks if a user is currently authenticated and logged in.
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise.
     */
    public static boolean isLoggedIn() {
        return UserDAO.isSignedIn();
    }

    /**
     * Logs out the currently authenticated user.
     * <p>
     * Clears associated session data from shared preferences.
     * </p>
     *
     * @param context The application context used to clear shared preferences.
     * @throws UserStateException if no user is currently logged in.
     */
    public static void logout(Context context) {
        if (isLoggedIn()) {
            UserDAO.signOut();
            PreferencesHelper.clearUserHeaders(context);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void setUsername(String newUsername) {
        if (isLoggedIn()) {
            UserDAO.setUsername(newUsername);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void getUserLinks(UserDAO.UserLinksCallback callback) {
        UserDAO.getUserLinks(callback);
    }

    /**
     * Retrieves the username of the authenticated user.
     *
     * @param callback A callback to handle the retrieved username.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUsername(UserDAO.UpdateUsernameViewCallback callback) {
        if (isLoggedIn()) {
            UserDAO.getUsername(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void setDefaultAnonymousPreference(Context context, boolean isChecked) {
        PreferencesHelper.setAnonymousPreferences(context, isChecked);
        UserDAO.setDefaultAnonymousPreference(isChecked);
    }

    public static void getDefaultAnonymousPreference(Context context, UserDAO.SettingsPreferencesToggle callback) {
        UserDAO.getDefaultAnonymousPreference((result) -> {
            // correct cached value using the result from the data base if necessary
            if (PreferencesHelper.getAnonymousPreferences(context) != result) {
                PreferencesHelper.setAnonymousPreferences(context, result);
            }

            callback.onComplete(result);
        });
    }

    /**
     * Retrieves the email of the authenticated user.
     *
     * @param callback A callback to handle the retrieved email.
     * @throws UserStateException if no user is logged in.
     */
    public static void getEmail(UserDAO.UpdateEmailViewCallback callback) {
        if (isLoggedIn()) {
            UserDAO.getEmail(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Retrieves user headers (username and email) from shared preferences.
     * If not locally available, fetches data from the backend.
     *
     * @param context  The application context to access shared preferences.
     * @param callback A callback to handle the retrieved headers.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserHeadersFromPreferences(Context context, UserHeadersCallback callback) {
        if (isLoggedIn()) {
            updateUserHeadersToPreferences(context, el ->
                    PreferencesHelper.checkOnPreferences(context, callback));
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void updateUserLinks(String link) {
        UserDAO.updateUserLinks(link);
    }

    /**
     * Updates the user headers in shared preferences to reflect the latest data.
     * <p>
     * Synchronizes preferences with the latest server-side data.
     * </p>
     *
     * @param context The application context to access shared preferences.
     * @throws UserStateException if no user is logged in.
     */
    public static void updateUserHeadersToPreferences(Context context, UserHeadersCallback callback) {
        String username;

        if (!isLoggedIn()) {
            throw new UserStateException("User is not signed in.");
        }

        username = PreferencesHelper.retrieveName(context);
        getUsername(usernameDB -> {
            if (!Objects.equals(usernameDB, username)) {
                PreferencesHelper.updateName(context, usernameDB);
            }

            String email = PreferencesHelper.retrieveEmail(context);
            getEmail(emailDB -> {
                if (!Objects.equals(email, emailDB)) {
                    PreferencesHelper.updateEmail(context, emailDB);
                }
            });

            // Make sure the user is in shared preferences
            PreferencesHelper.checkOnPreferences(context, callback);
        });
    }

    /**
     * Fetches the list of user notes from the backend.
     *
     * @param callback A callback to handle the retrieved notes list.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserNotesList(UserDAO.NotesListCallback callback) {
        if (isLoggedIn()) {
            UserDAO.getUserNotesList(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void getUserInfoByUID(String UID, UserDAO.GetUserInfoCallBack callback){
        UserDAO.getUserInfoByUID(UID, callback);
    }

    /**
     * Retrieves the unique user ID of the authenticated user.
     *
     * @return The UID of the authenticated user.
     */
    public static String getUid() {
        return UserDAO.getUid();
    }

    /**
     * Validates a callback to ensure it is not null.
     *
     * @param callback The callback to validate.
     * @throws IllegalArgumentException if the callback is {@code null}.
     */
    private static void validateCallback(Object callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null.");
        }
    }

    /**
     * Callback interface for receiving user header data.
     */
    public interface UserHeadersCallback {
        void onComplete(HashMap<String, String> headers);
    }

    /**
     * Exception class for handling user state-related errors.
     */
    public static class UserStateException extends RuntimeException {
        public UserStateException(String message) {
            super(message);
        }
    }
}
