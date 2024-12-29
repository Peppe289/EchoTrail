package com.peppe289.echotrail.controller.notes;

import com.google.firebase.firestore.GeoPoint;
import com.peppe289.echotrail.dao.notes.NotesDAO;
import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.HashMap;
import java.util.Map;

public class NotesController {

    public interface SaveNoteCallback {
        void onSavedNote();
    }

    public static void saveNote(Map<String, Object> data, SaveNoteCallback callback) {
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("userId", UserDAO.getUid());
        noteData.put("content", data.get("content"));

        if (data.get("latitude") != null && data.get("longitude") != null) {
            GeoPoint coordinates = new GeoPoint((double) data.get("latitude"), (double) data.get("longitude"));
            noteData.put("coordinates", coordinates);
        }

        noteData.put("timestamp", com.google.firebase.Timestamp.now());
        noteData.put("city", data.get("city"));

        NotesDAO.saveNote(noteData, callback);
    }

    public static void getAllNotes(UserDAO.NotesListCallback callback) {
        NotesDAO.getAllNotes(callback);
    }
}
