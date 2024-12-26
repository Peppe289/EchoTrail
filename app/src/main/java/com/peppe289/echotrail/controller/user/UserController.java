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

    public interface UserHeadersCallback {
        void onComplete(HashMap<String, String> headers);
    }

    /**
     * Logs in the user as a guest (anonymous authentication).
     *
     * @return {@code true} if the guest login was successful, {@code false} otherwise.
     */
    public static boolean loginAsGuest() {
        return UserDAO.signIn();
    }

    /**
     * Logs in the user using an email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    public static void login(String email, String password, UserDAO.SignInCallback callback) {
        UserDAO.signIn(email, password, callback);
    }

    /**
     * Registers a new user with the specified email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's username.
     * @param callback The callback to be invoked upon completion.
     */
    public static void register(String email, String password, String username, UserDAO.SignUpCallback callback) {
        UserDAO.signUp(email, password, username, callback);
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
     */
    public static void logout() {
        UserDAO.signOut();
    }

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * @param callback The callback to be invoked upon completion.
     */
    public static void getUsername(UserDAO.UpdateUsernameViewCallback callback) {
        UserDAO.getUsername(callback);
    }

    /**
     * Retrieves the email of the currently authenticated user.
     *
     * @param callback The callback to be invoked upon completion.
     */
    public static void getEmail(UserDAO.UpdateEmailViewCallback callback) {
        UserDAO.getUsername(callback);
    }

    /**
     * Retrieves the user headers from the shared preferences.
     */
    public static void getUserHeadersFromPreferences(Context context, UserHeadersCallback callback) {
        PreferencesHelper.checkOnPreferences(context, callback);
    }
}
