package com.peppe289.echotrail.dao.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.peppe289.echotrail.dao.notes.NotesDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The {@code UserDAO} class provides methods for user authentication and management using Firebase Authentication and Firestore.
 * It supports anonymous sign-in, email/password authentication, user registration, password reset, and user session management.
 *
 * <p><b>Note:</b> This implementation uses synchronous behavior with {@code CountDownLatch},
 * which may block the current thread. Ensure it is not used on the main UI thread to avoid freezing the application.</p>
 */
public class UserDAO {

    /**
     * The Firebase Authentication instance.
     */
    static private final FirebaseAuth auth = FirebaseAuth.getInstance();
    /**
     * The Firestore database instance.
     */
    static private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Signs in the user using email and password authentication.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback to be invoked upon completion.
     */
    public static void signIn(String email, String password, SignInCallback callback) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            callback.onComplete(task.isSuccessful());
        });
    }

    /**
     * Registers a new user with the provided email, password, and username.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's desired username.
     * @param callback The callback to be invoked upon completion.
     */
    public static void signUp(String email, String password, String username, SignUpCallback callback) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            db.collection("users").document(getUid()).set(new HashMap<String, Object>() {{
                put("username", username);
            }}).addOnCompleteListener(task1 -> {
                callback.onComplete(task.isSuccessful() && task1.isSuccessful());
            });
        });
    }

    public static void updateNotesList(String noteId) {
        db.collection("users")
                .document(getUid())
                .update("notes", FieldValue.arrayUnion(noteId));
    }

    /**
     * Signs out the currently authenticated user.
     */
    public static void signOut() {
        auth.signOut();
    }

    /**
     * Checks if a user is currently signed in.
     *
     * @return {@code true} if a user is signed in, {@code false} otherwise.
     */
    public static boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    /**
     * Retrieves the unique identifier (UID) of the currently authenticated user.
     *
     * @return The UID of the current user, or {@code null} if no user is signed in.
     */
    public static String getUid() {
        return Objects.requireNonNull(auth.getCurrentUser()).getUid();
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset link to.
     */
    public static void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email);
    }

    /**
     * Retrieves the username of the currently authenticated user from Firestore.
     *
     * @param callback The callback to be invoked with the retrieved username.
     */
    public static void getUsername(UpdateUsernameViewCallback callback) {
        db.collection("users").document(getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onComplete(Objects.requireNonNull(task.getResult()).getString("username"));
            }
        });
    }

    /**
     * Retrieves the email address of the currently authenticated user.
     *
     * @param callback The callback to be invoked with the retrieved email address.
     */
    public static void getEmail(UpdateEmailViewCallback callback) {
        String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        callback.onComplete(email);
    }

    public static void getUserNotesList(NotesListCallback callback) {
        try {
            db.collection("users")
                    .document(getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                List<String> notesID = (List<String>) documentSnapshot.get("notes");
                                NotesDAO.getNotes(notesID, callback);
                            } catch (Exception ignored) {
                                callback.onComplete(null);
                            }
                        }
                    });
        } catch (Exception ignored) {
            callback.onComplete(null);
        }
    }

    /**
     * Callback interface to handle the result of the sign-up process.
     */
    public interface SignUpCallback {
        /**
         * Called upon completion of the sign-up process.
         *
         * @param success {@code true} if sign-up was successful, {@code false} otherwise.
         */
        void onComplete(boolean success);
    }

    /**
     * Callback interface to handle the result of the sign-in process.
     */
    public interface SignInCallback {
        /**
         * Called upon completion of the sign-in process.
         *
         * @param success {@code true} if sign-in was successful, {@code false} otherwise.
         */
        void onComplete(boolean success);
    }

    /**
     * Callback interface to handle the result of username retrieval.
     */
    public interface UpdateUsernameViewCallback {
        /**
         * Called upon completion of the username retrieval process.
         *
         * @param name The username of the currently authenticated user.
         */
        void onComplete(String name);
    }

    /**
     * Callback interface to handle the result of email retrieval.
     */
    public interface UpdateEmailViewCallback {
        /**
         * Called upon completion of the email retrieval process.
         *
         * @param email The email address of the currently authenticated user.
         */
        void onComplete(String email);
    }

    public interface NotesListCallback {
        void onComplete(DocumentSnapshot notes);
    }
}
