package com.peppe289.echotrail.controller.notes;

import com.google.firebase.firestore.GeoPoint;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.notes.NotesDAO;
import com.peppe289.echotrail.dao.user.UserDAO;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@code NotesController} class provides high-level operations
 * for managing user notes in the EchoTrail application. It serves as
 * the link between the user interface or application logic and the
 * underlying data access layer managed by {@link NotesDAO}.
 * <p>
 * Core functionalities include:
 * <ul>
 *     <li>Saving user notes with optional geolocation data.</li>
 *     <li>Retrieving all notes from the backend.</li>
 * </ul>
 */
public class NotesController {

    /**
     * Saves a new note with optional geolocation and city data.
     * <p>
     * The method automatically associates the note with the currently authenticated user
     * by including their UID. It supports adding geolocation data (latitude and longitude),
     * a city name, and a timestamp.
     * </p>
     *
     * @param data     A map containing the note's data. The expected keys are:
     *                 <ul>
     *                     <li>"content" (String): The content of the note.</li>
     *                     <li>"latitude" (Double): The latitude of the note's location (optional).</li>
     *                     <li>"longitude" (Double): The longitude of the note's location (optional).</li>
     *                     <li>"city" (String): The name of the city where the note is created (optional).</li>
     *                 </ul>
     * @param callback A callback invoked upon successful saving of the note.
     */
    public static void saveNote(Map<String, Object> data, SaveNoteCallback callback) {
        Map<String, Object> noteData = new HashMap<>();

        // Add mandatory fields
        noteData.put("userId", UserDAO.getUid());
        noteData.put("content", data.get("content"));
        UserController.getUsername((username) -> {

        });

        try {
            String username = (String) data.get("username");
            if (username != null && !username.isEmpty())
                noteData.put("username", username);
        } catch (Exception ignored) {
        }

        // Add optional geolocation data if available
        if (data.get("latitude") != null && data.get("longitude") != null) {
            GeoPoint coordinates = new GeoPoint(
                    (double) data.get("latitude"),
                    (double) data.get("longitude")
            );
            noteData.put("coordinates", coordinates);
        }

        // Add timestamp and optional city name
        noteData.put("timestamp", com.google.firebase.Timestamp.now());
        noteData.put("city", data.get("city"));

        // Save the note through NotesDAO
        NotesDAO.saveNote(noteData, callback);
    }

    public static void updateReadNotesList(String noteId) {
        UserDAO.updateReadNotesList(noteId);
    }

    /**
     * Retrieves all notes available in the backend database.
     * <p>
     * The retrieved notes are provided via the callback interface {@link UserDAO.NotesListCallback}.
     * </p>
     *
     * @param callback A callback invoked with the list of notes retrieved.
     */
    public static void getAllNotes(UserDAO.NotesListCallback callback) {
        NotesDAO.getAllNotes(callback);
    }

    /**
     * A callback interface to handle the result of saving a note.
     */
    public interface SaveNoteCallback {
        /**
         * Called when the note has been successfully saved.
         */
        void onSavedNote();
    }
}
