package com.peppe289.echotrail.controller.user;

import android.content.Context;
import android.os.Build;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.exceptions.UserStateException;
import com.peppe289.echotrail.model.Session;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.ui.activity.MainActivity;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.IPGeolocation;
import com.peppe289.echotrail.utils.NavigationHelper;
import com.peppe289.echotrail.utils.UniqueIDHelper;
import com.peppe289.echotrail.utils.callback.IPGeolocationCallback;

import java.util.ArrayList;
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
     * @param context The application context to access shared preferences.
     * @throws UserStateException if no user is currently logged in.
     */
    public static void logout(Context context) {
        if (isLoggedIn()) {
            UserController.removeSession(UniqueIDHelper.getUniqueID(context), new ControllerCallback<Void, ErrorType>() {
                @Override
                public void onSuccess(Void result) {
                    PreferencesController.clearUserHeaders();
                    PreferencesController.clearAnonymousPreferences();
                    // go to login page.
                    NavigationHelper.rebaseActivity(context, MainActivity.class, null);
                    userDAO.signOut();
                }

                @Override
                public void onError(ErrorType error) {
                    // if we get some error, logout anyway
                    userDAO.signOut();
                }
            });
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
                callback.onError(ErrorType.UNKNOWN_ERROR);
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
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        });
    }

    public static void setDefaultAnonymousPreference(boolean isChecked) {
        PreferencesController.setAnonymousPreferences(isChecked);
        userDAO.setDefaultAnonymousPreference(isChecked);
    }

    public static void getDefaultAnonymousPreference(ControllerCallback<Boolean, ErrorType> callback) {
        boolean defaultPref = PreferencesController.getAnonymousPreferences();
        // preload info, if not in preferences
        callback.onSuccess(defaultPref);

        userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
            @Override
            public void onSuccess(User result) {
                callback.onSuccess(result.isAnonymousByDefault());
            }

            @Override
            public void onError(Exception error) {
                // if error, return the default value from share preferences (see below)
                // but notify always the error
                callback.onError(ErrorType.UNKNOWN_ERROR);
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
     * @param callback A callback to handle the retrieved headers.
     * @throws UserStateException if no user is logged in.
     */
    public static void getUserHeadersFromPreferences(ControllerCallback<HashMap<String, String>, ErrorType> callback) {
        if (isLoggedIn()) {
            // make 2 time. the first help in use case like start up op application (retrieve from preferences if isn't empty)
            // the second help in case of change from database (update preferences) in async way
            PreferencesController.loadFromPreferences(callback);
            updateUserHeadersToPreferences(new ControllerCallback<HashMap<String, String>, ErrorType>() {
                @Override
                public void onSuccess(HashMap<String, String> result) {
                    PreferencesController.loadFromPreferences(callback);
                }

                @Override
                public void onError(ErrorType error) {
                    callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            });
        }
    }

    public static void addSession(Context context, ControllerCallback<Void, ErrorType> callback) {
        String androidID = UniqueIDHelper.getUniqueID(context);
        IPGeolocation.getCountryFromIP(new IPGeolocationCallback<String, ErrorType>() {
            @Override
            public void onSuccess(String result) {
                Session session = new Session(
                        Build.MANUFACTURER + " " + Build.MODEL,
                        result,
                        Timestamp.now(),
                        Build.VERSION.RELEASE);
                userDAO.addSession(session, androidID, new UserCallback<Void, Exception>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(Exception error) {
                        callback.onError(ErrorType.ADD_SESSION_ERROR);
                    }
                });
            }

            @Override
            public void onError(ErrorType error) {
                // ignore. never happen
            }
        });
    }

    public static void getAllSessions(ControllerCallback<List<Session>, ErrorType> callback) {
        userDAO.getAllSessions(new UserCallback<QuerySnapshot, Exception>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                List<Session> sessionList = new ArrayList<>();

                for (DocumentSnapshot document : querySnapshot) {
                    if (document.exists()) {
                        Session session = document.toObject(Session.class);
                        if (session != null) {
                            session.setId(document.getId());
                            sessionList.add(session);
                        }
                    }
                }

                callback.onSuccess(sessionList);
            }

            @Override
            public void onError(Exception error) {
                // TODO: create right error code
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        });
    }

    public static void removeSession(String session, ControllerCallback<Void, ErrorType> callback) {
        userDAO.removeSession(session, new UserCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                // TODO: create right error code.
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        });

    }

    /**
     * Make this function with observer in DAO. In this way we are able to reuse code.
     * Indeed, this can make logout without more code, but only with login page code in {@link MainActivity}.
     *
     * @param devUID        The unique code of this login.
     * @param callback      The callback function.
     */
    public static void checkValidSession(String devUID, ControllerCallback<Boolean, ErrorType> callback) {
        UserCallback<QuerySnapshot, Exception> userCallback = new UserCallback<>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                // nothing happen
                callback.onSuccess(true);
            }

            @Override
            public void onError(Exception error) {
                // TODO: replace with right method.
                // this cause logout.
                callback.onError(ErrorType.UNKNOWN_ERROR);
            }
        };

        userDAO.sessionListener(new UserCallback<QuerySnapshot, Exception>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                userDAO.checkValidSession(devUID, userCallback);
            }

            @Override
            public void onError(Exception error) {
                userDAO.checkValidSession(devUID, userCallback);
            }
        });
    }

    public static void changePassword(String oldPassword, String newPassword, ControllerCallback<Void, ErrorType> callback) {
        UserController.getEmail(new ControllerCallback<String, ErrorType>() {
            @Override
            public void onSuccess(String result) {
                userDAO.changePassword(EmailAuthProvider.getCredential(result, oldPassword) , newPassword, new UserCallback<Void, Exception>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(Exception error) {
                        callback.onError(ErrorType.CHANGE_PASSWORD_ERROR);
                    }
                });
            }

            @Override
            public void onError(ErrorType error) {
                callback.onError(error);
            }
        });
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
     * @throws UserStateException if no user is logged in.
     */
    public static void updateUserHeadersToPreferences(ControllerCallback<HashMap<String, String>, ErrorType> callback) {
        if (!isLoggedIn()) {
            throw new UserStateException("User is not signed in.");
        }

        userDAO.genericUserListener(new UserCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                userDAO.getEmail(new UserCallback<String, Exception>() {
                    @Override
                    public void onSuccess(String email) {
                        PreferencesController.updateEmail(email);
                        userDAO.getUserInfo(getUid(), new UserCallback<User, Exception>() {
                            @Override
                            public void onSuccess(User result) {
                                String username = result.getUsername();
                                PreferencesController.updateName(username);
                                callback.onSuccess(new HashMap<>(2) {{
                                    put("username", username);
                                    put("email", email);
                                }});
                            }

                            @Override
                            public void onError(Exception error) {
                                callback.onError(ErrorType.UNKNOWN_ERROR);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        callback.onError(ErrorType.UNKNOWN_ERROR);
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.UNKNOWN_ERROR);
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
                callback.onError(ErrorType.UNKNOWN_ERROR);
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
