package com.peppe289.echotrail.dao.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.controller.notes.NotesController;

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

    public void getUserLinks(UserLinksCallback callback) {
        getUserLinks(getUid(), callback);
    }

    public void getUserLinks(String userID, UserLinksCallback callback) {
        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            /* consider this always as list of string. */
                            @SuppressWarnings("unchecked")
                            List<String> links = (List<String>) documentSnapshot.get("links");
                            callback.onComplete(links);
                        } catch (Exception ignored) {
                            callback.onComplete(null);
                        }
                    }
                });
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

    public void getReadedNotesList(NotesListCallback callback) {
        try {
            db.collection("users")
                    .document(getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                /* consider this always as list of string. */
                                @SuppressWarnings("unchecked")
                                List<String> notesID = (List<String>) documentSnapshot.get("readedNotes");
                                NotesController.getNotes(notesID, callback);
                            } catch (Exception ignored) {
                                callback.onComplete(null);
                            }
                        }
                    });
        } catch (Exception ignored) {
            callback.onComplete(null);
        }
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

    public void getUsername(UpdateUsernameViewCallback callback) {
        db.collection("users").document(getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onComplete(Objects.requireNonNull(task.getResult()).getString("username"));
            }
        });
    }

    public void setUsername(String username) {
        db.collection("users").document(getUid()).update("username", username);
    }

    public void getUserInfoByUID(String UID, GetUserInfoCallBack callback) {
        HashMap<String, Object> userInfo = new HashMap<>();
        try {
            db.collection("users")
                    .document(UID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                /* consider this always as list of string. */
                                @SuppressWarnings("unchecked")
                                List<String> notesID = (List<String>) documentSnapshot.get("notes");
                                /* consider this always as list of string. */
                                @SuppressWarnings("unchecked")
                                List<String> readedNotesID = (List<String>) documentSnapshot.get("readedNotes");
                                userInfo.put("notes", notesID == null ? 0 : notesID.size());
                                userInfo.put("readedNotes", readedNotesID == null ? 0 : readedNotesID.size());
                                userInfo.put("username", documentSnapshot.getString("username"));
                                userInfo.put("links", documentSnapshot.get("links"));
                                callback.onComplete(userInfo);
                            } catch (Exception ignored) {
                                callback.onComplete(null);
                            }
                        }
                    });
        } catch (Exception ignored) {
            callback.onComplete(null);
        }
    }

    public void getEmail(UpdateEmailViewCallback callback) {
        String email = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        callback.onComplete(email);
    }

    public void getUserNotesList(NotesListCallback callback) {
        try {
            db.collection("users")
                    .document(getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            try {
                                /* consider this always as list of string. */
                                @SuppressWarnings("unchecked")
                                List<String> notesID = (List<String>) documentSnapshot.get("notes");
                                NotesController.getNotes(notesID, callback);
                            } catch (Exception ignored) {
                                callback.onComplete(null);
                            }
                        }
                    });
        } catch (Exception ignored) {
            callback.onComplete(null);
        }
    }

    public void setDefaultAnonymousPreference(boolean isAnonymous) {
        db.collection("users")
                .document(getUid())
                .update("anonymousByDefault", isAnonymous);
    }

    public void getDefaultAnonymousPreference(SettingsPreferencesToggle callback) {
        db.collection("users")
                .document(getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            boolean isAnonymous = Boolean.TRUE.equals(documentSnapshot.getBoolean("anonymousByDefault"));
                            callback.onComplete(isAnonymous);
                        } catch (Exception ignored) {
                            callback.onComplete(false);
                        }
                    }
                });
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
    }

    public interface SettingsPreferencesToggle {
        void onComplete(boolean isAnonymous);
    }

    public interface UserLinksCallback {
        void onComplete(List<String> links);
    }

    public interface GetUserInfoCallBack {
        void onComplete(HashMap<String, Object> userInfo);
    }
}
