package com.peppe289.echotrail.dao.user;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.peppe289.echotrail.controller.user.UserController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String currentUserId = userDAO.getUid();
        String friendshipId = currentUserId + "_" + friendId;

        Map<String, Object> data = new HashMap<>();
        data.put("from", currentUserId);
        data.put("to", friendId);
        data.put("date", System.currentTimeMillis());

        db.collection("friends")
                .document(friendshipId)
                .set(data, SetOptions.merge()) // Usa merge per non sovrascrivere altri campi
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
    public void rejectRequest(String friendID, RemoveFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        db.collection("friends")
                .document(friendID + "_" + userDAO.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> callback.onFriendRemoved())
                .addOnFailureListener(e -> callback.onFriendRemoved());
    }

    /**
     * Removes a friend from the user's friend list.
     * PLEASE DON'T TRY TO "OPTIMIZE" THIS METHOD BY REWORKING THE FOR LOOP! I LOST 3 DAY TO UNDERSTAND WHY IT DOESN'T WORK!
     * I HATE JAVA! For remove this object from array need the right instance of the object, not only the same value...
     *
     * @param friendID The ID of the friend to be removed.
     * @param callback The callback to be invoked upon completion.
     */
    public void removeFriend(String friendID, RemoveFriendCallback callback) {
        UserDAO userDAO = new UserDAO();
        String uid = userDAO.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> friends = (List<String>) documentSnapshot.get("friends");
                    if (friends != null) {
                        for (String odioJava : friends) {
                            if (odioJava.trim().compareTo(friendID.trim()) == 0) {
                                friends.remove(odioJava);
                                db.collection("users").document(uid)
                                        .update("friends", friends)
                                        .addOnSuccessListener(aVoid -> callback.onFriendRemoved())
                                        .addOnFailureListener(e -> callback.onFriendRemoved());
                                return;
                            }
                        }
                    }
                    callback.onFriendRemoved();
                }).addOnFailureListener(e -> callback.onFriendRemoved());
    }

    public void searchPendingRequests(GetFriendsCallback callback) {
        UserDAO userDAO = new UserDAO();
        db.collection("friends")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> pendingRequest = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String documentId = document.getId();
                        if (documentId.endsWith("_" + userDAO.getUid())) {
                            pendingRequest.add(documentId.substring(0, documentId.length() - (userDAO.getUid().length() + 1)));
                        }
                    }

                    callback.onFriendsRetrieved(pendingRequest);
                })
                .addOnFailureListener(e -> callback.onFriendsRetrieved(null));
    }

    /**
     * Synchronizes the user's friends list with the database.
     * This method is used to ensure that the user's friends list is up-to-date.
     *
     * @param runnable The callback to be invoked upon completion
     *                 if the friend is accepted, add him to my friend list!!!
     *
     */
    public void synchronizeFriendsList(Runnable runnable) {
        UserDAO userDAO = new UserDAO();
        // just ignore the user which send me the request.
        // we need later
        List<String> ignoreList = new ArrayList<>();

        // this snippet is used to check if my friends request are accepted!!
        db.collection("friends")
                // get all friends pending request
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    for (DocumentSnapshot document : documentSnapshot) {
                        String documentId = document.getId();
                        if (documentId.startsWith(userDAO.getUid() + "_")) {
                            // the name of request id should be: myID_friendID
                            // take the friendID and check if he accepts the request (he has me in his friend list)
                            String friendID = documentId.substring(userDAO.getUid().length() + 1).trim();
                            checkIfFriendIsAccepted(friendID, runnable);
                        }


                        // this request is for me, i can ignore this.
                        if (documentId.endsWith("_" + userDAO.getUid())) {
                            // the name of request id should be: friendID_myID
                            // add this to ignore list
                            ignoreList.add(documentId.substring(0, documentId.length() - (userDAO.getUid().length() + 1)));
                        }
                    }

                    // remove friends if he removed me from his friend list!!!
                    // strunz e merd.
                    db.collection("users")
                            .document(userDAO.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot1 -> {
                                List<String> friends = (List<String>) documentSnapshot1.get("friends");
                                if (friends == null)
                                    return;
                                for (String brother : friends) {

                                    boolean checkThisStrunz = true;

                                    for (String ignore : ignoreList) {
                                        if (ignore.compareTo(brother) == 0) {
                                            checkThisStrunz = false;
                                            break;
                                        }
                                    }

                                    if (!checkThisStrunz)
                                        continue;

                                    db.collection("user")
                                            .document(brother)
                                            .get().addOnSuccessListener(brotherData -> {
                                                List<String> brotherFriends = (List<String>) brotherData.get("friends");
                                                // my bro don't have friends, also removed me.
                                                if (brotherFriends == null) {
                                                    // sei rimasto solo stronzo
                                                    removeFriend(brother, () -> {
                                                    });
                                                    return;
                                                }
                                                boolean remove = true;
                                                for (String fr : brotherFriends) {
                                                    // my friend have me in his friend list, so I can't remove him.
                                                    // ti amo pasticcino <3
                                                    if (fr.compareTo(userDAO.getUid()) == 0) {
                                                        remove = false;
                                                        break;
                                                    }
                                                }

                                                if (remove) {
                                                    removeFriend(brother, () -> {
                                                    });
                                                }
                                            });
                                }
                            });


                    runnable.run();
                }).addOnFailureListener(e -> runnable.run());
    }

    /**
     * Check if the friend is accepted.
     *
     * @param friendID check if the friend is accepted
     * @param runnable callback to be invoked upon completion
     *                 if the friend is accepted, add him to my friend list!!!
     */
    private void checkIfFriendIsAccepted(String friendID, Runnable runnable) {
        UserDAO userDAO = new UserDAO();

        getUIDFriendsList(friendID, friends -> {
            if (friends != null) {
                for (String fr : friends) {
                    if (fr.compareTo(userDAO.getUid()) == 0) {
                        // I'm in his friend list, so I can add him to my friend list
                        db.collection("users")
                                .document(userDAO.getUid())
                                .update("friends", FieldValue.arrayUnion(friendID))
                                .addOnSuccessListener(unused -> {
                                   db.collection("friends")
                                           .document(userDAO.getUid() + "_" + friendID)
                                           .delete().addOnSuccessListener(unused1 -> runnable.run());
                                });
                        return; // well if I'm checked I can stop the loop.
                    }
                }
            }
        });
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

    public interface RemoveFriendCallback {
        void onFriendRemoved();
    }
}
