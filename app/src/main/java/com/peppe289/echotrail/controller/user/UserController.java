package com.peppe289.echotrail.controller.user;

import android.content.Context;

import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.HashMap;

/**
 * The {@code UserController} class serves as an intermediary between the application logic
 * and the data access layer for user authentication and management.
 * <p>
 * It provides high-level methods for logging in, registering, checking user status,
 * and logging out, delegating the actual implementation to the {@code UserDAO}.
 * </p>
 */
public class UserController {

    /**
     * Logs in the user using an email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback to be invoked upon completion.
     * @throws UserStateException if the user is already signed in.
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
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's username.
     * @param callback The callback to be invoked upon completion.
     * @throws UserStateException if the user is already signed in.
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
     * Checks if a user is currently logged in.
     *
     * @return {@code true} if a user is logged in, {@code false} otherwise.
     */
    public static boolean isLoggedIn() {
        return UserDAO.isSignedIn();
    }

    /**
     * Logs out the currently authenticated user.
     *
     * @param context The context to access shared preferences.
     * @throws UserStateException if the user is not signed in.
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
     * Retrieves the username of the currently authenticated user.
     *
     * @param callback The callback to be invoked upon completion.
     * @throws UserStateException if the user is not signed in.
     */
    public static void getUsername(UserDAO.UpdateUsernameViewCallback callback) {
        validateCallback(callback);

        if (isLoggedIn()) {
            UserDAO.getUsername(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Retrieves the email of the currently authenticated user.
     *
     * @param callback The callback to be invoked upon completion.
     * @throws UserStateException if the user is not signed in.
     */
    public static void getEmail(UserDAO.UpdateEmailViewCallback callback) {
        validateCallback(callback);

        if (isLoggedIn()) {
            UserDAO.getEmail(callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Retrieves the user headers from shared preferences.
     *
     * @param context  The context to access shared preferences.
     * @param callback The callback to be invoked with the user headers.
     * @throws UserStateException if the user is not signed in.
     */
    public static void getUserHeadersFromPreferences(Context context, UserHeadersCallback callback) {
        validateCallback(callback);

        if (isLoggedIn()) {
            PreferencesHelper.checkOnPreferences(context, callback);
        } else {
            throw new UserStateException("User is not signed in.");
        }
    }

    /**
     * Validates that the provided callback is not null.
     *
     * @param callback The callback to validate.
     * @throws IllegalArgumentException if the callback is null.
     */
    private static void validateCallback(Object callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null.");
        }
    }

    /**
     * Callback interface for retrieving user headers.
     */
    public interface UserHeadersCallback {
        void onComplete(HashMap<String, String> headers);
    }

    /**
     * Custom exception class for handling user state-related errors.
     */
    public static class UserStateException extends RuntimeException {
        public UserStateException(String message) {
            super(message);
        }
    }
}
