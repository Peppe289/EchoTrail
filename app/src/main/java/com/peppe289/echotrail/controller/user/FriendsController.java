package com.peppe289.echotrail.controller.user;

import com.peppe289.echotrail.dao.user.FriendsDAO;
import com.peppe289.echotrail.exceptions.FriendNotFoundException;
import com.peppe289.echotrail.utils.ControllerCallback;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.concurrent.atomic.AtomicBoolean;

public class FriendsController {
    private static FriendsDAO friendsDAO;

    public static void init() {
        friendsDAO = new FriendsDAO();
    }

    public static void init(FriendsDAO friendsDAO) {
        FriendsController.friendsDAO = friendsDAO;
    }

    public static void searchPendingRequests(FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.searchPendingRequests(callback);
    }

    public static void requestToBeFriends(String friendId, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.requestToBeFriends(friendId, callback);
    }

    public static void acceptRequest(String friendID, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.acceptRequest(friendID, callback);
    }

    /**
     * Removes a friend from the friends list or pending list.
     * Don't care about if is in pending request or friends list.
     * Just remove in both if exists.
     * This approach is used for simplify the code and logic and
     * improve stability.
     *
     * @param friendID id of the friend to be removed
     * @param callback callback to be invoked upon completion
     */
    public static void removeFriend(String friendID, FriendsDAO.RemoveFriendCallback callback) {
        AtomicBoolean atLastOne = new AtomicBoolean(false);

        ControllerCallback<String, Exception> controllerCallback = new ControllerCallback<String, Exception>() {
            @Override
            public void onSuccess(String result) {
                callback.onFriendRemoved();
            }

            @Override
            public void onError(Exception error) {
                if (atLastOne.get()) {
                    callback.onFriendRemoved();
                } else {
                    if (error instanceof FriendNotFoundException) {
                        callback.onError(ErrorType.FRIEND_NOT_FOUND_ERROR);
                    } else {
                        callback.onError(ErrorType.REMOVE_FRIEND_ERROR);
                    }
                }
            }
        };

        friendsDAO.removeFriend(friendID, new ControllerCallback<String, Exception>() {
            @Override
            public void onSuccess(String result) {
                // at last the first request work.
                atLastOne.set(true);
                friendsDAO.rejectRequest(friendID, controllerCallback);
            }

            @Override
            public void onError(Exception error) {
                // also if removeFriend fail, I run anyway rejectRequest
                friendsDAO.rejectRequest(friendID, controllerCallback);
            }
        });
    }

    public static void getUIDFriendsList(String userID, FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.getUIDFriendsList(userID, callback);
    }

    public static void getUIDFriendsList(FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.getUIDFriendsList(callback);
    }
}
