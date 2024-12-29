package com.peppe289.echotrail.dao.notes;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.peppe289.echotrail.controller.notes.NotesController;
import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.List;
import java.util.Map;

public class NotesDAO {
    /**
     * The Firebase Authentication instance.
     */
    static private final FirebaseAuth auth = FirebaseAuth.getInstance();
    /**
     * The Firestore database instance.
     */
    static private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveNote(Map<String, Object> noteData,
                                NotesController.SaveNoteCallback callback) {
        db.collection("notes")
                .add(noteData)
                .addOnSuccessListener(documentReference -> {
                    String noteId = documentReference.getId();
                    callback.onSavedNote();
                    UserDAO.updateNotesList(noteId);
                });
    }

    public static void getNotes(List<String> notesID, UserDAO.NotesListCallback callback) {
        db.collection("notes")
                .whereIn(FieldPath.documentId(), notesID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Call here callback function
                    for (DocumentSnapshot noteDoc : querySnapshot) {
                        callback.onComplete(noteDoc);
                    }
                })
                .addOnFailureListener(e -> Log.e("Notes", "Error getting notes: " + e.getMessage()));
    }

    public static void getAllNotes(UserDAO.NotesListCallback callback) {
        db.collection("notes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Call here callback function
                    for (DocumentSnapshot noteDoc : querySnapshot) {
                        callback.onComplete(noteDoc);
                    }
                })
                .addOnFailureListener(e -> Log.e("Notes", "Error getting notes: " + e.getMessage()));
    }
}
