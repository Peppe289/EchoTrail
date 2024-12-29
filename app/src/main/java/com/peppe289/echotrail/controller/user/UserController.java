package com.peppe289.echotrail.controller.user;

import android.content.Context;

import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.HashMap;
import java.util.Objects;

/**
 * The {@code UserController} class serves as a bridge between the application logic
 * and the data access layer for user-related operations, such as authentication,
 * user data management, and session handling.
 * <p>
 * This class provides high-level methods for:
 * <ul>
 *     <li>User login and logout</li>
 *     <li>User registration</li>
 *     <li>Retrieving user information (e.g., username, email)</li>
 *     <li>Managing user session data through shared preferences</li>
 * </ul>
 * Each operation delegates implementation details to {@code UserDAO} and manages application-level validation.
 */
public class UserController {

    /**
     * Logs in the user with the provided email and password.
     * <p>
     * This method ensures that no user is already logged in before initiating the login process.
     * If the login is successful, the provided {@link UserDAO.SignInCallback} is invoked.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback A callback to handle the login response.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the callback is {@code null}.
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
     * Registers a new user with the specified email, password, and username.
     * <p>
     * The method ensures that no user is already logged in before attempting to register a new account.
     * Upon successful registration, the provided {@link UserDAO.SignUpCallback} is invoked.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's desired username.
     * @param callback A callback to handle the registration response.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the callback is {@code null}.
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
     * Checks whether a user is currently logged in.
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise.
     */
    public static boolean isLoggedIn() {
        return UserDAO.isSignedIn();
    }

    /**
     * Logs out the currently authenticated user and clears associated session data.
     * <p>
     * This method ensures that a user is logged in before attempting to log out.
     * It also clears user-related data stored in shared preferences.
     * </p>
     *
     * @param context The application context used to access shared preferences.
     * @throws UserStateException if no user is currently logged in.
     */
    public static void logout(Context context) {
        if (isLoggedIn()) {
            UserDAO.signOut();
            PreferencesHelper.clearUserHeaders(context); // Clear user headers from shared preferences
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Retrieves the username of the currently logged-in user.
     *
     * @param callback A callback to receive the username data.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUsername(UserDAO.UpdateUsernameViewCallback callback) {
        if (isLoggedIn()) {
            UserDAO.getUsername(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Retrieves the email of the currently logged-in user.
     *
     * @param callback A callback to receive the email data.
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
     * Retrieves user headers (e.g., username and email) from shared preferences.
     * If the data is not present locally, it attempts to fetch it from the server.
     *
     * @param context  The application context used to access shared preferences.
     * @param callback A callback to receive the user headers as a map.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserHeadersFromPreferences(Context context, UserHeadersCallback callback) {
        if (isLoggedIn()) {
            PreferencesHelper.checkOnPreferences(context, callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Updates the user headers (e.g., username and email) stored in shared preferences.
     * <p>
     * This method synchronizes the shared preferences with the latest user data
     * retrieved from the server, if there are discrepancies.
     * </p>
     *
     * @param context The application context used to access shared preferences.
     * @throws UserStateException if no user is logged in.
     */
    public static void updateUserHeadersToPreferences(Context context) {
        String username;
        String email;

        if (!isLoggedIn()) {
            throw new UserStateException("User is not signed in.");
        }

        username = PreferencesHelper.retrieveName(context);
        getUsername(usernameDB -> {
            if (Objects.equals(usernameDB, username)) {
                PreferencesHelper.updateName(context, usernameDB);
            }
        });

        email = PreferencesHelper.retrieveEmail(context);
        getEmail(emailDB -> {
            if (Objects.equals(email, emailDB)) {
                PreferencesHelper.updateEmail(context, emailDB);
            }
        });

        PreferencesHelper.checkOnPreferences(context, null);
    }

    /**
     * Validates the provided callback to ensure it is not null.
     *
     * @param callback The callback to validate.
     * @throws IllegalArgumentException if the callback is {@code null}.
     */
    private static void validateCallback(Object callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null.");
        }
    }

    public static void getUserNotesList(UserDAO.NotesListCallback callback) {
        if (isLoggedIn()) {
            UserDAO.getUserNotesList(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    public static String getUid() {
        return UserDAO.getUid();
    }

    /**
     * A callback interface for receiving user headers.
     */
    public interface UserHeadersCallback {
        void onComplete(HashMap<String, String> headers);
    }

    /**
     * A custom exception class for handling errors related to user state.
     */
    public static class UserStateException extends RuntimeException {
        public UserStateException(String message) {
            super(message);
        }
    }
}
