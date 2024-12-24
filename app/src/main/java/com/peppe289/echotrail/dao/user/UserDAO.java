package com.peppe289.echotrail.dao.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * The {@code UserDAO} class provides methods to handle user authentication and management
 * using Firebase Authentication. It supports anonymous sign-in, email/password authentication,
 * user registration, password reset, and user session management.
 *
 * <p><b>Note:</b> This implementation uses synchronous behavior with {@code CountDownLatch},
 * which may block the current thread. Ensure it is not used on the main UI thread to avoid freezing the application.</p>
 */
@SuppressWarnings("unused")
public class UserDAO {

    public interface SignUpCallback {
        void onComplete(boolean success);
    }

    /**
     * The Firebase Authentication instance.
     */
    static private final FirebaseAuth auth = FirebaseAuth.getInstance();
    /**
     * The Firestore database instance.
     */
    static private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Signs in the user anonymously using Firebase Authentication.
     *
     * @return {@code true} if the sign-in operation was successful, {@code false} otherwise.
     */
    public static boolean signIn() {
        final boolean[] result = {false};
        CountDownLatch latch = new CountDownLatch(1);

        auth.signInAnonymously().addOnCompleteListener(task -> {
            result[0] = task.isSuccessful();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        return result[0];
    }

    /**
     * Signs in the user using email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return {@code true} if the sign-in operation was successful, {@code false} otherwise.
     */
    public static boolean signIn(String email, String password) {
        final boolean[] result = {false};
        CountDownLatch latch = new CountDownLatch(1);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            result[0] = task.isSuccessful();
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        return result[0];
    }

    /**
     * Registers a new user with the provided email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param username The user's username.
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
     * Retrieves the email address of the currently authenticated user.
     *
     * @return The email address of the current user, or {@code null} if no user is signed in.
     */
    public static String getEmail() {
        return Objects.requireNonNull(auth.getCurrentUser()).getEmail();
    }

    /**
     * Sends a password reset email to the specified email address.
     *
     * @param email The email address to send the password reset link to.
     */
    public static void sendPasswordResetEmail(String email) {
        auth.sendPasswordResetEmail(email);
    }
}