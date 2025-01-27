package com.peppe289.echotrail.dao.user;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.peppe289.echotrail.controller.user.UserController;

import java.util.ArrayList;
import java.util.List;

public class FriendsDAO {
    private final FirebaseFirestore db;

    public FriendsDAO() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * When a user sends a friend request to another user, this method is called to add the friend to
     * the user's pending friend list.
     *
     * @param friendId The ID of the user to whom the friend request is sent.
     * @param callback The callback to be invoked upon completion.
     */
    public void requestToBeFriends(String friendId, AddFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        // Add friend to user's friend list
        db.collection("friends")
                .document(userDAO.getUid() + "_" + friendId)
                .update("from", userDAO.getUid(), "to", friendId,
                        "date", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> callback.onFriendAdded(true))
                .addOnFailureListener(e -> callback.onFriendAdded(false));
    }

    /**
     * Accepts a friend request from another user. This method removes the friend request
     * from the pending list and adds the friend to the user's friend list.
     *
     * @param friendID The ID of the user whose friend request is being accepted.
     * @param callback The callback to be invoked upon completion.
     */
    public void acceptRequest(String friendID, AddFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        // Add friend to user's friend list
        db.collection("friends")
                .document(userDAO.getUid() + "_" + friendID)
                .delete()
                .addOnSuccessListener(aVoid ->
                        db.collection("users")
                                .document(userDAO.getUid())
                                .update("friends", FieldValue.arrayUnion(friendID))
                                .addOnSuccessListener(aVoid1 ->
                                        callback.onFriendAdded(true)))
                .addOnFailureListener(e -> callback.onFriendAdded(false));
    }

    /**
     * Rejects a friend request from another user.
     * This method remove the friend request.
     *
     * @param friendID The ID of the user whose friend request is being rejected.
     * @param callback The callback to be invoked upon completion.
     */
    public void rejectRequest(String friendID, AddFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        db.collection("friends")
                .document(userDAO.getUid() + "_" + friendID)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onFriendAdded(true))
                .addOnFailureListener(e -> callback.onFriendAdded(false));
    }

    public void removeFriend(String friendID, AddFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        db.collection("users")
                .document(userDAO.getUid())
                .update("friends", FieldValue.arrayRemove(friendID))
                .addOnSuccessListener(aVoid -> callback.onFriendAdded(true))
                .addOnFailureListener(e -> callback.onFriendAdded(false));
    }

    public void searchPendingRequest(GetFriendsCallback callback) {
        UserDAO userDAO = new UserDAO();
        db.collection("friends")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> pendingRequest = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String documentId = document.getId();
                        if (documentId.endsWith("_" + userDAO.getUid())) {
                            pendingRequest.add(documentId.substring(0, documentId.length() - (userDAO.getUid().length() - 1)));
                        }
                    }

                    callback.onFriendsRetrieved(pendingRequest);
                })
                .addOnFailureListener(e -> callback.onFriendsRetrieved(null));
    }

    /**
     * If the user remove one friend, the friend must remove the user from his friend list.
     * This method is called to synchronize the friend list of the user with the given ID.
     * For first, check if the mine friend list contains the mine ID, if isn't present,
     * this isn't my friends. Then remove also from my list if is present.
     *
     * @param runnable The callback to be invoked upon completion.
     */
    public void synchronizeFriendsList(Runnable runnable) {
        UserDAO userDAO = new UserDAO();

        db.collection("users")
                .document(userDAO.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> friendsList = (List<String>) documentSnapshot.get("friends");

                    if (friendsList == null) {
                        runnable.run();
                        return;
                    }

                    searchPendingRequest(pendingRequest -> {
                        for (String friend : friendsList) {
                            db.collection("users")
                                    .document(friend)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot2 -> {
                                        List<String> friendsList2 = (List<String>) documentSnapshot2.get("friends");

                                        if (friendsList2 != null && !friendsList2.contains(userDAO.getUid())) {
                                            // this isn't my friend (maybe jet? let's see in pending request).
                                            if (pendingRequest != null && !pendingRequest.contains(friend)) {
                                                // this isn't my friend, remove from my list.
                                                db.collection("users")
                                                        .document(userDAO.getUid())
                                                        .update("friends", FieldValue.arrayRemove(friend));
                                            }
                                        }


                                    });
                        }
                    });
                    runnable.run();
                }).addOnFailureListener(e -> runnable.run());
    }

    public void getUIDFriendsList(GetFriendsCallback callback) {
        // Synchronize the friend list before getting it
        synchronizeFriendsList(() -> getUIDFriendsList(UserController.getUid(), callback));
    }

    /**
     * Get the list of friends of the user with the given ID.
     *
     * @param userID   The ID of the user whose friends are to be retrieved.
     * @param callback The callback to be invoked upon completion.
     */
    public void getUIDFriendsList(String userID, GetFriendsCallback callback) {
        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null) {
                        callback.onFriendsRetrieved(null);
                    } else {
                        List<String> friends = (List<String>) queryDocumentSnapshots.get("friends");
                        callback.onFriendsRetrieved(friends);
                    }
                })
                .addOnFailureListener(e -> callback.onFriendsRetrieved(null));
    }

    public interface GetFriendsCallback {
        void onFriendsRetrieved(List<String> friends);
    }

    public interface AddFriendCallback {
        void onFriendAdded(boolean success);
    }
}
