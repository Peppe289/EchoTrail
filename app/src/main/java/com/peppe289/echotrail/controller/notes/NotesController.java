package com.peppe289.echotrail.controller.notes;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.NotesCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.notes.NotesDAO;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.HashMap;
import java.util.List;
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

    private static NotesDAO notesDAO;

    public static void init() {
        init(NotesDAO.getInstance());
    }

    public static void init(NotesDAO notesDAO) {
        NotesController.notesDAO = notesDAO;
    }

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

        Object countryObj = data.get("country");
        if (countryObj == null) {
            callback.onSavedNote(ErrorType.SAVE_NOTE_FAILED);
            return;
        }

        String country = countryObj.toString();

        // Add mandatory fields
        noteData.put("userId", UserController.getUid());
        noteData.put("content", data.get("content"));

        try {
            String username = (String) data.get("username");
            if (username != null && !username.isEmpty())
                noteData.put("username", username);
        } catch (Exception ignored) {
        }

        try {
            String sendTo = (String) data.get("send_to");
            if (sendTo != null && !sendTo.isEmpty())
                noteData.put("send_to", sendTo);
        } catch (Exception ignored) {
        }

        // Add optional geolocation data if available
        if (data.get("latitude") != null && data.get("longitude") != null) {
            Double latitude = (Double) data.get("latitude");
            Double longitude = (Double) data.get("longitude");
            GeoPoint coordinates = null;
            if (latitude != null && longitude != null) {
                coordinates = new GeoPoint(latitude, longitude);
            }
            noteData.put("coordinates", coordinates);
        }

        // Add timestamp and optional city name
        noteData.put("timestamp", com.google.firebase.Timestamp.now());
        noteData.put("city", data.get("city"));

        // Save the note through NotesDAO
        notesDAO.saveNote(noteData, country, new NotesCallback<String, Exception>() {
            @Override
            public void onSuccess(String noteId) {
                callback.onSavedNote(null);
                UserController.updateNotesList(noteId);
            }

            @Override
            public void onError(Exception error) {
                callback.onSavedNote(ErrorType.SAVE_NOTE_FAILED);
            }
        });
    }

    public static void getNotes(List<String> notesID, ControllerCallback<QuerySnapshot, ErrorType> callback) {
        notesDAO.getNotes(notesID, new NotesCallback<QuerySnapshot, Exception>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.GET_USER_NOTES_ERROR);
            }
        });
    }

    public static void updateReadNotesList(String noteId) {
        UserController.updateReadNotesList(noteId);
    }

    /**
     * Retrieves all notes available in the backend database.
     * <p>
     * The retrieved notes are provided via the callback interface {@link ControllerCallback}.
     * </p>
     *
     * @param callback A callback invoked with the list of notes retrieved.
     */
    public static void getAllNotes(String country, ControllerCallback<QuerySnapshot, ErrorType> callback) {
        getAllNotes(country, callback, false);
    }

    public static void getAllNotes(String country, ControllerCallback<QuerySnapshot, ErrorType> callback, boolean listen) {
        notesDAO.getAllNotes(country, new NotesCallback<QuerySnapshot, Exception>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.GET_USER_NOTES_ERROR);
            }
        }, listen);
    }

    /**
     * A callback interface to handle the result of saving a note.
     */
    public interface SaveNoteCallback {
        /**
         * Called when the note has been successfully saved.
         */
        void onSavedNote(ErrorType errorType);
    }
}
