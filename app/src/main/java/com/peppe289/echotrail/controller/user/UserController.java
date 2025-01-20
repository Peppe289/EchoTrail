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
     * User DAO object for handling user-related operations.
     */
    private static UserDAO userDAO;

    /**
     * Initializes the {@link UserDAO} instance for user-related operations.
     */
    public static void init() {
        init(new UserDAO());
    }

    public static void init(UserDAO userDAO) {
        UserController.userDAO = userDAO;
    }

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
            userDAO.signIn(email, password, callback);
        } else {
            throw new UserStateException("User is already signed in.");
        }
    }

    public static void getReadedNotesList(UserDAO.NotesListCallback callback) {
        userDAO.getReadedNotesList(callback);
    }

    public static void updateNotesList(String noteId) {
        userDAO.updateNotesList(noteId);
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
            userDAO.signUp(email, password, username, callback);
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
        return userDAO.isSignedIn();
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
            userDAO.signOut();
            PreferencesHelper.clearUserHeaders(context);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void setUsername(String newUsername) {
        if (isLoggedIn()) {
            userDAO.setUsername(newUsername);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void getUserLinks(UserDAO.UserLinksCallback callback) {
        userDAO.getUserLinks(callback);
    }

    public static void getUserLinks(String userID, UserDAO.UserLinksCallback callback) {
        userDAO.getUserLinks(userID, callback);
    }

    /**
     * Retrieves the username of the authenticated user.
     *
     * @param callback A callback to handle the retrieved username.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUsername(UserDAO.UpdateUsernameViewCallback callback) {
        if (isLoggedIn()) {
            userDAO.getUsername(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void setDefaultAnonymousPreference(Context context, boolean isChecked) {
        PreferencesHelper.setAnonymousPreferences(context, isChecked);
        userDAO.setDefaultAnonymousPreference(isChecked);
    }

    public static void getDefaultAnonymousPreference(Context context, UserDAO.SettingsPreferencesToggle callback) {
        userDAO.getDefaultAnonymousPreference((result) -> {
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
            userDAO.getEmail(callback);
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
            // make 2 time. the first help in use case like start up op application (retrieve from preferences if isn't empty)
            // the second help in case of change from database (update preferences) in async way
            PreferencesHelper.checkOnPreferences(context, callback);
            updateUserHeadersToPreferences(context, el ->
                    PreferencesHelper.checkOnPreferences(context, callback));
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void updateUserLinks(String link) {
        userDAO.updateUserLinks(link);
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
            userDAO.getUserNotesList(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static void getUserInfoByUID(String UID, UserDAO.GetUserInfoCallBack callback){
        userDAO.getUserInfoByUID(UID, callback);
    }

    /**
     * Retrieves the unique user ID of the authenticated user.
     *
     * @return The UID of the authenticated user.
     */
    public static String getUid() {
        return userDAO.getUid();
    }


    public static void updateReadNotesList(String noteId) {
        userDAO.updateReadNotesList(noteId);
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
