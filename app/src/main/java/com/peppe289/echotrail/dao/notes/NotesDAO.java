package com.peppe289.echotrail.dao.notes;

import androidx.annotation.Nullable;
import com.google.firebase.firestore.*;
import com.peppe289.echotrail.annotations.TestOnly;
import com.peppe289.echotrail.controller.callback.NotesCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.NoteCollectionException;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.utils.FirestoreConstants;

import java.util.List;
import java.util.Map;

/**
 * A Data Access Object (DAO) class for managing operations related to notes in a Firebase Firestore database.
 * <p>
 * This class provides methods to save, retrieve, and manage notes stored in the Firestore database.
 * It also integrates with the {@link UserDAO} to update user-specific note lists.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Saves new notes to the Firestore database.</li>
 *     <li>Fetches specific notes by their unique IDs.</li>
 *     <li>Retrieves all notes stored in the database.</li>
 *     <li>Utilizes Firebase Authentication to associate notes with authenticated users.</li>
 * </ul>
 * </p>
 */
public class NotesDAO {
    /**
     * The Firestore database instance used for accessing and managing notes.
     */
    private final FirebaseFirestore db;
    private static NotesDAO instance;

    @TestOnly
    private NotesDAO(FirebaseFirestore db) {
        this.db = db;
    }

    private NotesDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized NotesDAO getInstance() {
        if (instance == null) {
            instance = new NotesDAO();
        }
        return instance;
    }

    /**
     * Saves a new note to the Firestore database.
     * <p>
     * Upon successful saving, this method triggers a callback and updates the user's list of notes
     * through the {@link UserDAO}.
     * </p>
     *
     * @param noteData a {@link Map} containing the note's data (e.g., title, content, metadata)
     * @param callback a callback instance to notify when the save operation is complete
     */
    public void saveNote(Map<String, Object> noteData, NotesCallback<String, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_NOTES)
                .add(noteData)
                .addOnSuccessListener(documentReference -> {
                    String noteId = documentReference.getId();
                    callback.onSuccess(noteId);
                })
                .addOnFailureListener(e ->
                        callback.onError(new NoteCollectionException()));
    }

    /**
     * Retrieves a list of notes from the Firestore database by their unique IDs.
     * <p>
     * This method queries the "notes" collection using the provided list of note IDs and triggers
     * the provided callback for each note retrieved successfully.
     * </p>
     *
     * @param notesID  a {@link List} of note document IDs to fetch
     * @param callback a callback instance to handle the retrieved notes
     */
    public void getNotes(List<String> notesID, NotesCallback<QuerySnapshot, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_NOTES)
                .whereIn(FieldPath.documentId(), notesID)
                .get()
                .addOnSuccessListener(callback::onSuccess);
    }

    /**
     * Retrieves all notes from the Firestore database.
     * <p>
     * This method queries the "notes" collection and triggers the provided callback for each note
     * retrieved successfully.
     * </p>
     *
     * @param callback a callback instance to handle the retrieved notes
     */
    public void getAllNotes(NotesCallback<QuerySnapshot, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_NOTES)
                .get()
                .addOnSuccessListener(callback::onSuccess);
    }

    /**
     * Retrieves all notes from the Firestore database using listener on snapshot update.
     *
     * @param callback
     * @param listen for change signature
     */
    public void getAllNotes(NotesCallback<QuerySnapshot, Exception> callback, boolean listen) {
        if (!UserController.isLoggedIn())
            return;

        if (listen)
            db.collection(FirestoreConstants.COLLECTION_NOTES).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }

                    callback.onSuccess(value);
                }
            });
        else
            getAllNotes(callback);
    }
}
