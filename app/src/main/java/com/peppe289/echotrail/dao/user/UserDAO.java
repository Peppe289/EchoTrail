package com.peppe289.echotrail.dao.user;

import android.icu.text.IDNA;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.annotations.TestOnly;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.utils.ErrorType;
import com.peppe289.echotrail.utils.FirestoreConstants;

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
    private static UserDAO instance;

    @TestOnly
    public UserDAO(FirebaseAuth auth, FirebaseFirestore db) {
        this.auth = auth;
        this.db = db;
    }

    /**
     * Constructor for UserDAO. Initializes FirebaseAuth and FirebaseFirestore instances.
     */
    private UserDAO() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    /**
     * Signs in the user using email and password authentication.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @param callback The callback to be invoked upon completion.
     */
    public void signIn(String email, String password, UserCallback<Void, Exception> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result ->
                        callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void signUp(String email, String password, String username, UserCallback<Void, Exception> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->
                        db.collection(FirestoreConstants.COLLECTION_USERS)
                                .document(getUid())
                                .update(FirestoreConstants.Users.FIELD_USERNAME, username)
                                .addOnCompleteListener(task1 ->
                                        callback.onSuccess(null))
                                .addOnFailureListener(callback::onError))
                .addOnFailureListener(callback::onError);
    }

    public void updateNotesList(String noteId) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(getUid())
                .update(FirestoreConstants.Users.FIELD_PUBLISHED_NOTES, FieldValue.arrayUnion(noteId));
    }

    public void updateReadNotesList(String noteId) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(getUid())
                .update(FirestoreConstants.Users.FIELD_READED_NOTES, FieldValue.arrayUnion(noteId));
    }

    public void updateUserLinks(String link) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(getUid())
                .update(FirestoreConstants.Users.FIELD_LINKS, FieldValue.arrayUnion(link));
    }

    public void removeUserLink(String link) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(getUid())
                .update(FirestoreConstants.Users.FIELD_LINKS, FieldValue.arrayRemove(link));
    }

    public void signOut() {
        auth.signOut();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getUid() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        return firebaseUser != null ? firebaseUser.getUid() : "";
    }

    public void getUserInfo(String uid, UserCallback<User, Exception> callback) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.toObject(User.class);
                    callback.onSuccess(user);
                }).addOnFailureListener(snapshot ->
                        callback.onError(new UserCollectionException()));
    }

    public void setUsername(String username) {
        db.collection(FirestoreConstants.COLLECTION_USERS).document(getUid()).update(FirestoreConstants.Users.FIELD_USERNAME, username);
    }

    public void getEmail(UserCallback<String, Exception> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
            callback.onSuccess(user.getEmail());
        else
            callback.onError(new UserCollectionException());
    }

    public void setDefaultAnonymousPreference(boolean isAnonymous) {
        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(getUid())
                .update(FirestoreConstants.Users.FIELD_PREF_ANONYMOUS, isAnonymous);
    }
}
