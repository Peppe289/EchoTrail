package com.peppe289.echotrail.dao.user;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;
import com.peppe289.echotrail.annotations.TestOnly;
import com.peppe289.echotrail.controller.callback.FriendsCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.exceptions.FriendCollectionException;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.utils.FirestoreConstants;

import java.util.*;
import java.util.stream.Collectors;

public class FriendsDAO {
    private final FirebaseFirestore db;
    private final UserDAO userDAO;
    private static FriendsDAO instance;

    @TestOnly
    public FriendsDAO(UserDAO userDAO, FirebaseFirestore fb) {
        this.userDAO = userDAO;
        this.db = fb;
    }

    private FriendsDAO() {
        this.userDAO = UserDAO.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized FriendsDAO getInstance() {
        if (instance == null) {
            instance = new FriendsDAO();
        }
        return instance;
    }

    /**
     * When a user sends a friend request to another user, this method is called to add the friend to
     * the user's pending friend list.
     *
     * @param friendId The ID of the user to whom the friend request is sent.
     * @param callback The callback to be invoked upon completion.
     */
    public void requestToBeFriends(String friendId, FriendsCallback<Void, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        String currentUserId = userDAO.getUid();
        String friendshipId = currentUserId + "_" + friendId;

        Map<String, Object> data = new HashMap<>();
        data.put(FirestoreConstants.Friends.FIELD_SENDER, currentUserId);
        data.put(FirestoreConstants.Friends.FIELD_RECEIVER, friendId);
        data.put(FirestoreConstants.Friends.FIELD_DATE, System.currentTimeMillis());

        db.collection(FirestoreConstants.COLLECTION_FRIENDS)
                .document(friendshipId)
                .set(data, SetOptions.merge()) // Usa merge per non sovrascrivere altri campi
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }


    /**
     * Accepts a friend request from another user. This method removes the friend request
     * from the pending list and adds the friend to the user's friend list.
     *
     * @param friendID The ID of the user whose friend request is being accepted.
     * @param callback The callback to be invoked upon completion.
     */
    public void acceptRequest(String friendID, FriendsCallback<Void, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        WriteBatch batch = db.batch();

        DocumentReference requestRef = db.collection(FirestoreConstants.COLLECTION_FRIENDS).document(userDAO.getUid() + "_" + friendID);
        DocumentReference userRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(userDAO.getUid());
        batch.delete(requestRef);
        batch.update(userRef, FirestoreConstants.COLLECTION_FRIENDS, FieldValue.arrayUnion(friendID));

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }

    /**
     * Rejects a friend request from another user.
     * This method remove the friend request.
     *
     * @param friendID The ID of the user whose friend request is being rejected.
     * @param callback The callback to be invoked upon completion.
     */
    public void rejectRequest(String friendID, FriendsCallback<String, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_FRIENDS)
                .document(friendID + "_" + userDAO.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(friendID))
                .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }

    /**
     * Removes a friend from the user's friend list.
     *
     * @param pointerString The ID of the friend to be removed.
     * @param callback The callback to be invoked upon completion.
     */
    public void removeFriend(String pointerString, FriendsCallback<String, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        String uid = userDAO.getUid();

        db.collection(FirestoreConstants.COLLECTION_USERS).document(uid)
                .update(FirestoreConstants.COLLECTION_FRIENDS, FieldValue.arrayRemove(pointerString))
                .addOnSuccessListener(aVoid -> callback.onSuccess(pointerString))
                .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }

    public void searchPendingRequests(FriendsCallback<List<String>, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_FRIENDS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null) {
                        callback.onSuccess(null);
                        return;
                    }

                    List<String> pendingRequest = querySnapshot.getDocuments().stream()
                            .map(DocumentSnapshot::getId) // Map each DocumentSnapshot to its ID
                            .filter(documentId -> documentId.endsWith("_" + userDAO.getUid())) // Filter out requests sent by the user
                            .map(documentId -> documentId.substring(0, documentId.length() - (userDAO.getUid().length() + 1))) // Process the ID further
                            .collect(Collectors.toList());

                    callback.onSuccess(pendingRequest);
                })
                .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }

    /**
     * Synchronizes the user's friends list with the database.
     * Ensures that:
     * - Friend requests are checked for acceptance.
     * - Friends who removed the user are also removed.
     * - Pending friend requests are NOT deleted until the sender sees they were accepted.
     *
     * @param callback The callback to be invoked upon completion.
     */
    public void synchronizeFriendsList(FriendsCallback<Void, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        String myUid = userDAO.getUid();
        Set<String> ignoreList = new HashSet<>();
        List<Task<Void>> pendingTasks = new ArrayList<>();

        // Fetch all friend requests
        db.collection(FirestoreConstants.COLLECTION_FRIENDS).get().addOnSuccessListener(requestsSnapshot -> {
            for (DocumentSnapshot document : requestsSnapshot) {
                String documentId = document.getId();

                if (documentId.startsWith(myUid + "_")) {
                    // Request sent by me (format: myID_friendID)
                    String friendID = documentId.substring(myUid.length() + 1).trim();
                    pendingTasks.add(checkIfFriendIsAccepted(friendID, true)); // Now deletes request after acceptance
                }

                if (documentId.endsWith("_" + myUid)) {
                    // Request received (format: friendID_myID) → This means a friend request is pending
                    String friendID = documentId.substring(0, documentId.length() - (myUid.length() + 1));
                    ignoreList.add(friendID);
                }
            }

            // Fetch my friends list
            db.collection(FirestoreConstants.COLLECTION_USERS).document(myUid).get().addOnSuccessListener(userSnapshot -> {
                List<String> myFriends = (List<String>) userSnapshot.get(FirestoreConstants.COLLECTION_FRIENDS);
                if (myFriends == null || myFriends.isEmpty()) {
                    Tasks.whenAllComplete(pendingTasks)
                            .addOnCompleteListener(task -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onError(new Exception()));
                    return;
                }

                for (String friendID : new ArrayList<>(myFriends)) {
                    if (ignoreList.contains(friendID)) {
                        // Friend request is pending, do nothing
                        continue;
                    }

                    pendingTasks.add(db.collection(FirestoreConstants.COLLECTION_USERS).document(friendID).get().continueWithTask(task -> {
                        if (!task.isSuccessful() || !task.getResult().exists()) {
                            return removeFriend(friendID);
                        }

                        List<String> friendFriends = (List<String>) task.getResult().get(FirestoreConstants.COLLECTION_FRIENDS);
                        if (friendFriends == null || !friendFriends.contains(myUid)) {
                            return removeFriend(friendID);
                        }

                        return Tasks.forResult(null); // No need to remove
                    }));
                }

                // Execute the callback once all tasks are completed
                Tasks.whenAllComplete(pendingTasks)
                        .addOnCompleteListener(task -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
            });
        }).addOnFailureListener(e -> callback.onError(new FriendCollectionException()));
    }

    /**
     * Checks if a friend request has been accepted and adds them as a friend if so.
     * If accepted, the request is deleted from the FirestoreConstants.COLLECTION_FRIENDS collection.
     *
     * @param friendID      The friend's user ID.
     * @param deleteRequest If true, deletes the friend request after acceptance.
     * @return A Firestore Task to track completion.
     */
    private Task<Void> checkIfFriendIsAccepted(String friendID, boolean deleteRequest) {
        DocumentReference friendRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(friendID);
        return friendRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                return Tasks.forResult(null);
            }

            List<String> friendFriends = (List<String>) task.getResult().get(FirestoreConstants.COLLECTION_FRIENDS);
            if (friendFriends != null && friendFriends.contains(userDAO.getUid())) {
                Task<Void> addFriendTask = addFriend(friendID);

                if (deleteRequest) {
                    return addFriendTask.continueWithTask(t -> deleteFriendRequest(friendID));
                }

                return addFriendTask;
            }

            return Tasks.forResult(null);
        });
    }

    /**
     * Removes a friend from the user's friend list.
     *
     * @param friendID The ID of the friend to remove.
     * @return A Firestore Task to track completion.
     */
    private Task<Void> removeFriend(String friendID) {
        DocumentReference userRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(userDAO.getUid());
        return userRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) return Tasks.forException(Objects.requireNonNull(task.getException()));
            List<String> friends = (List<String>) task.getResult().get(FirestoreConstants.COLLECTION_FRIENDS);
            if (friends == null || !friends.contains(friendID)) return Tasks.forResult(null);

            friends.remove(friendID);
            return userRef.update(FirestoreConstants.COLLECTION_FRIENDS, friends);
        });
    }

    /**
     * Adds a friend to the user's friend list.
     *
     * @param friendID The ID of the friend to add.
     * @return A Firestore Task to track completion.
     */
    private Task<Void> addFriend(String friendID) {
        DocumentReference userRef = db.collection(FirestoreConstants.COLLECTION_USERS).document(userDAO.getUid());
        return userRef.get().continueWithTask(task -> {
            if (!task.isSuccessful()) return Tasks.forException(Objects.requireNonNull(task.getException()));
            List<String> friends = (List<String>) task.getResult().get(FirestoreConstants.COLLECTION_FRIENDS);
            if (friends == null) friends = new ArrayList<>();

            if (!friends.contains(friendID)) {
                friends.add(friendID);
                return userRef.update(FirestoreConstants.COLLECTION_FRIENDS, friends);
            }
            return Tasks.forResult(null);
        });
    }

    /**
     * Deletes the friend request from the FirestoreConstants.COLLECTION_FRIENDS collection.
     *
     * @param friendID The ID of the friend whose request should be deleted.
     * @return A Firestore Task to track completion.
     */
    private Task<Void> deleteFriendRequest(String friendID) {
        DocumentReference requestRef = db.collection(FirestoreConstants.COLLECTION_FRIENDS).document(userDAO.getUid() + "_" + friendID);
        return requestRef.delete();
    }

    /**
     * Get the list of friends of the user with the given ID.
     *
     * @param userID   The ID of the user whose friends are to be retrieved.
     * @param callback The callback to be invoked upon completion.
     */
    public void getUIDFriendsList(String userID, FriendsCallback<List<String>, Exception> callback) {
        if (!UserController.isLoggedIn())
            return;

        db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null) {
                        callback.onSuccess(null);
                    } else {
                        List<String> friends = (List<String>) queryDocumentSnapshots.get(FirestoreConstants.COLLECTION_FRIENDS);
                        callback.onSuccess(friends);
                    }
                })
                .addOnFailureListener(e -> callback.onError(new UserCollectionException()));
    }
}
