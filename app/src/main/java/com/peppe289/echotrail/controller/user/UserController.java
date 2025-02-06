package com.peppe289.echotrail.controller.user;

import android.content.Context;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.exceptions.UserStateException;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.HashMap;
import java.util.List;
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
        init(UserDAO.getInstance());
    }

    public static void init(UserDAO userDAO) {
        UserController.userDAO = userDAO;
    }

    /**
     * Logs in a user using their email and password.
     * <p>
     * This method ensures no user is already logged in before initiating the login process.
     * On successful login, it invokes the provided {@link ControllerCallback}.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback A callback for handling the login result.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the provided callback is {@code null}.
     */
    public static void login(String email, String password, ControllerCallback<Void, ErrorType> callback) {
        validateCallback(callback);

        userDAO.signIn(email, password, new UserCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        });
    }

    public static void getReadedNotesList(ControllerCallback<QuerySnapshot, ErrorType> callback) {
        userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User user) {
                NotesController.getNotes(user.getReadedNotes(), callback);
            }

            @Override
            public void onError(Exception error) {
                if (error instanceof UserCollectionException)
                    callback.onError(ErrorType.GET_USER_READ_NOTES_ERROR);
            }
        });
    }

    public static void updateNotesList(String noteId) {
        userDAO.updateNotesList(noteId);
    }

    public static void removeUserLink(String link) {
        userDAO.removeUserLink(link);
    }

    /**
     * Registers a new user with the provided credentials and username.
     * <p>
     * Ensures no user is logged in before attempting to register a new account.
     * On successful registration, invokes the provided {@link ControllerCallback}.
     * </p>
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's desired username.
     * @param callback A callback for handling the registration result.
     * @throws UserStateException       if a user is already logged in.
     * @throws IllegalArgumentException if the provided callback is {@code null}.
     */
    public static void register(String email, String password, String username, ControllerCallback<Void, ErrorType> callback) {
        validateCallback(callback);

        if (!isLoggedIn()) {
            userDAO.signUp(email, password, username, new UserCallback<Void, Exception>() {
                @Override
                public void onSuccess(Void result) {
                    callback.onSuccess(null);
                }

                @Override
                public void onError(Exception error) {
                    callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            });
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

    public static void getUserLinks(ControllerCallback<List<String>, ErrorType> callback) {
        getUserLinks(getUid(), callback);
    }

    public static void getUserLinks(String userID, ControllerCallback<List<String>, ErrorType> callback) {
        userDAO.getUserInfo(userID, new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user.getLinks());
            }

            @Override
            public void onError(Exception error) {
                // TODO: do nothing for now. maybe we should handle this case
            }
        });
    }

    /**
     * Retrieves the username of the authenticated user.
     *
     * @param callback A callback to handle the retrieved username.
     */
    public static void getUsername(ControllerCallback<String, ErrorType> callback) {
        userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(result.getUsername());
            }

            @Override
            public void onError(Exception error) {
                // TODO: add handler error
            }
        });
    }

    public static void setDefaultAnonymousPreference(Context context, boolean isChecked) {
        PreferencesHelper.setAnonymousPreferences(context, isChecked);
        userDAO.setDefaultAnonymousPreference(isChecked);
    }

    public static void getDefaultAnonymousPreference(Context context, ControllerCallback<Boolean, ErrorType> callback) {
        boolean defaultPref = PreferencesHelper.getAnonymousPreferences(context);
        // preload info, if not in preferences
        callback.onSuccess(defaultPref);

        userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(result.isAnonymousByDefault());
            }

            @Override
            public void onError(Exception error) {
                // TODO: handle error
                // if error, return the default value from share preferences (see below)
            }
        });
    }

    /**
     * Retrieves the email of the authenticated user.
     *
     * @param callback A callback to handle the retrieved email.
     */
    public static void getEmail(ControllerCallback<String, ErrorType> callback) {
        userDAO.getEmail(new UserCallback<String, Exception>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        });
    }

    /**
     * Retrieves user headers (username and email) from shared preferences.
     * If not locally available, fetches data from the backend.
     *
     * @param context  The application context to access shared preferences.
     * @param callback A callback to handle the retrieved headers.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserHeadersFromPreferences(Context context, ControllerCallback<HashMap<String, String>, ErrorType> callback) {
        if (isLoggedIn()) {
            // make 2 time. the first help in use case like start up op application (retrieve from preferences if isn't empty)
            // the second help in case of change from database (update preferences) in async way
            PreferencesHelper.checkOnPreferences(context, callback);
            updateUserHeadersToPreferences(context, new ControllerCallback<HashMap<String, String>, ErrorType>() {
                @Override
                public void onSuccess(HashMap<String, String> result) {
                    PreferencesHelper.checkOnPreferences(context, callback);
                }

                @Override
                public void onError(ErrorType error) {
                    callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            });
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
    public static void updateUserHeadersToPreferences(Context context, ControllerCallback<HashMap<String, String>, ErrorType> callback) {
        String username;

        if (!isLoggedIn()) {
            throw new UserStateException("User is not signed in.");
        }

        username = PreferencesHelper.retrieveName(context);
        getUsername(new ControllerCallback<String, ErrorType>() {
            @Override
            public void onSuccess(String usernameDB) {
                if (!Objects.equals(usernameDB, username)) {
                    PreferencesHelper.updateName(context, usernameDB);
                }

                String email = PreferencesHelper.retrieveEmail(context);
                getEmail(new ControllerCallback<String, ErrorType>() {
                    @Override
                    public void onSuccess(String result) {
                        if (!Objects.equals(email, result)) {
                            PreferencesHelper.updateEmail(context, result);
                        }
                    }

                    @Override
                    public void onError(ErrorType error) {
                        // TODO: handle error
                    }
                });

                // Make sure the user is in shared preferences
                PreferencesHelper.checkOnPreferences(context, callback);
            }

            @Override
            public void onError(ErrorType error) {
                // TODO: handle error
            }
        });
    }

    /**
     * Fetches the list of user notes from the backend.
     *
     * @param callback A callback to handle the retrieved notes list.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserNotesList(ControllerCallback<QuerySnapshot, ErrorType> callback) {
        if (isLoggedIn())
            userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
                @Override
                public void onSuccess(User result) {
                    NotesController.getNotes(result.getNotes(), callback);
                }

                @Override
                public void onError(Exception error) {
                    if (error instanceof UserCollectionException)
                        callback.onError(ErrorType.GET_USER_NOTES_ERROR);
                    else
                        callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            });
        else
            callback.onError(ErrorType.USER_NOT_LOGGED_IN_ERROR);
    }

    public static void getUserInfoByUID(String uid, ControllerCallback<User, ErrorType> callback) {
        userDAO.getUserInfo(uid, new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception error) {
                ///  TODO: complete handle error
            }
        });
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
}
