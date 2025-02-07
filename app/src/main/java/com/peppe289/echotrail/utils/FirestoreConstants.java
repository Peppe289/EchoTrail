package com.peppe289.echotrail.utils;

/**
 * Centralized class for Firestore collection names and document field keys used in EchoTrail.
 * This class ensures consistency across Firestore operations and minimizes the risk of typos.
 * <p>
 * EchoTrail is an application that allows users to leave geolocated messages (notes)
 * in real-world locations. This class helps manage Firestore collections related to:
 * <ul>
 *     <li>Users and their profiles</li>
 *     <li>Notes left in various locations</li>
 *     <li>Friend requests: after request is accepted, the sender id is appended in Users.uid.friends list.</li>
 * </ul>
 */
public class FirestoreConstants {
    /**
     * Firestore collection name for storing user profiles.
     * <p>
     * Each document in this collection represents a single user identified by their unique user ID.
     * It stores user-related information such as their username, friends' list, published notes,
     * and user preferences.
     */
    public static final String COLLECTION_USERS = "users";

    /**
     * Firestore collection name for storing notes created by users.
     * <p>
     * Each document represents a unique note left by a user at a specific location.
     * Notes can be public or private and may contain textual or audio content.
     */
    public static final String COLLECTION_NOTES = "notes";

    /**
     * Firestore collection name for managing friendships and friend requests.
     * <p>
     * Each document represents a relationship between two users. It tracks whether a
     * friend request is pending, accepted, or rejected.
     */
    public static final String COLLECTION_FRIENDS = "friends";

    /**
     * Defines field names for documents stored in the "users" collection.
     * <p>
     * Each document in this collection represents an individual user profile.
     */
    public static class Users {

        /**
         * The username of the user.
         * <p>
         * This field is unique for each user and is used for identification within the app.
         */
        public static final String FIELD_USERNAME = "username";

        /**
         * A list of user IDs representing the user's friends.
         * <p>
         * This field contains the list of users that the current user is connected with as friends.
         */
        public static final String FIELD_FRIENDS = "friends";

        /**
         * A list of note IDs representing the notes that the user has read.
         * <p>
         * Used to track which notes have already been seen by the user.
         */
        public static final String FIELD_READED_NOTES = "readedNotes";

        /**
         * A list of note IDs representing the notes that the user has published.
         * <p>
         * This field stores references to all the notes created by the user.
         */
        public static final String FIELD_PUBLISHED_NOTES = "notes";

        /**
         * A list of external links associated with the user (for example, social media, website).
         * <p>
         * Users may add personal links to their profile for sharing additional content.
         */
        public static final String FIELD_LINKS = "links";

        /**
         * User preference indicating whether notes should be posted anonymously by default.
         * <p>
         * If set to true, notes will be posted without displaying the author's name.
         */
        public static final String FIELD_PREF_ANONYMOUS = "anonymousByDefault";
    }

    /**
     * Defines field names for documents stored in the "notes" collection.
     * <p>
     * Each document in this collection represents an individual note left by a user.
     */
    public static class Notes {

        /**
         * The city where the note was created.
         * <p>
         * This field allows filtering and searching for notes based on location.
         */
        public static final String FIELD_CITY = "city";

        /**
         * The textual content of the note.
         * <p>
         * This field stores the main body of the message left by the user.
         */
        public static final String FIELD_CONTENT = "content";

        /**
         * The geographical coordinates (latitude and longitude) where the note was placed.
         * <p>
         * Stored as a GeoPoint object to allow location-based queries.
         */
        public static final String FIELD_COORDINATES = "coordinates";

        /**
         * The timestamp indicating when the note was created.
         * <p>
         * This field is used to order notes chronologically.
         */
        public static final String FIELD_TIMESTAMP = "timestamp";

        /**
         * The unique ID of the user who created the note.
         * <p>
         * This field allows tracing each note to its author.
         */
        public static final String FIELD_USER_ID = "userId";

        /**
         * The ID of the user to whom the note is sent (for private notes).
         * <p>
         * If this field is null, the note is public.
         */
        public static final String FIELD_SEND_TO = "send_to";

        /**
         * The username of the note's author.
         * <p>
         * This field allows displaying the author's name without requiring a separate user query.
         */
        public static final String FIELD_USERNAME = "username";
    }

    /**
     * Defines field names for documents stored in the "friends" collection.
     * <p>
     * Each document represents a pending friend request between two users.
     */
    public static class Friends {

        /**
         * The user ID of the person who sent the friend request.
         * <p>
         * This field stores the ID of the user initiating the friendship.
         */
        public static final String FIELD_SENDER = "from";

        /**
         * The user ID of the person who received the friend request.
         * <p>
         * This field stores the ID of the user receiving the request.
         */
        public static final String FIELD_RECEIVER = "to";

        /**
         * The timestamp when the friend request was sent.
         * <p>
         * Used to order and track pending requests over time.
         */
        public static final String FIELD_DATE = "date";
    }
}
