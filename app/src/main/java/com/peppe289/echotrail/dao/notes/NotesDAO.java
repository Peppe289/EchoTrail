package com.peppe289.echotrail.dao.notes;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.peppe289.echotrail.controller.notes.NotesController;

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
                .addOnSuccessListener(documentReference ->
                        callback.onSavedNote());
    }
}
