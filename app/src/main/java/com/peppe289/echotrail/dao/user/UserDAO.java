package com.peppe289.echotrail.dao.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The {@code UserDAO} class provides methods for user authentication and management using Firebase Authentication and Firestore.
 * It supports anonymous sign-in, email/password authentication, user registration, password reset, and user session management.
 *
 * <p><b>Note:</b> This implementation is designed to be instantiated, providing more flexibility for dependency injection and testing.</p>
 */
public class UserDAO {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    /**
     * Constructor for UserDAO. Initializes FirebaseAuth and FirebaseFirestore instances.
     */
    public UserDAO() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Signs in the user using email and password authentication.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback to be invoked upon completion.
     */
    public void signIn(String email, String password, SignInCallback callback) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    public void signUp(String email, String password, String username, SignUpCallback callback) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> db.collection("users").document(getUid()).set(new HashMap<String, Object>() {{
            put("username", username);
        }}).addOnCompleteListener(task1 -> callback.onComplete(task.isSuccessful() && task1.isSuccessful())));
    }

    public void updateNotesList(String noteId) {
        db.collection("users")
                .document(getUid())
                .update("notes", FieldValue.arrayUnion(noteId));
    }

    public void updateReadNotesList(String noteId) {
        db.collection("users")
                .document(getUid())
                .update("readedNotes", FieldValue.arrayUnion(noteId));
    }

    public void updateUserLinks(String link) {
        db.collection("users")
                .document(getUid())
                .update("links", FieldValue.arrayUnion(link));
    }

    public void removeUserLink(String link) {
        db.collection("users")
                .document(getUid())
                .update("links", FieldValue.arrayRemove(link));
    }

    public void signOut() {
        auth.signOut();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getUid() {
        return Objects.requireNonNull(auth.getCurrentUser()).getUid();
    }

    public void getUserInfo(String uid, UserCallback<User, Exception> callback) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.toObject(User.class);
                    callback.onSuccess(user);
                }).addOnFailureListener(snapshot ->
                        callback.onError(new UserCollectionException()));
    }

    public void setUsername(String username) {
        db.collection("users").document(getUid()).update("username", username);
    }

    public void getEmail(UpdateEmailViewCallback callback) {
        String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        callback.onComplete(email);
    }

    public void setDefaultAnonymousPreference(boolean isAnonymous) {
        db.collection("users")
                .document(getUid())
                .update("anonymousByDefault", isAnonymous);
    }

    public interface SignUpCallback {
        void onComplete(boolean success);
    }

    public interface SignInCallback {
        void onComplete(boolean success);
    }

    public interface UpdateUsernameViewCallback {
        void onComplete(String name);
    }

    public interface UpdateEmailViewCallback {
        void onComplete(String email);
    }

    public interface NotesListCallback {
        void onComplete(QuerySnapshot notes);
        void onError(ErrorType errorType);
    }

    public interface SettingsPreferencesToggle {
        void onComplete(boolean isAnonymous);
    }

    public interface UserLinksCallback {
        void onComplete(List<String> links);
    }

    public interface GetUserInfoCallBack {
        void onComplete(User user);
    }
}
