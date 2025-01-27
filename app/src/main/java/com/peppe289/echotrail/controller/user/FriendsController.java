package com.peppe289.echotrail.controller.user;

import com.peppe289.echotrail.dao.user.FriendsDAO;

public class FriendsController {
    private static FriendsDAO friendsDAO;

    public static void init() {
        friendsDAO = new FriendsDAO();
    }

    public static void init(FriendsDAO friendsDAO) {
        FriendsController.friendsDAO = friendsDAO;
    }

    public static void requestToBeFriends(String friendId, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.requestToBeFriends(friendId, callback);
    }

    public static void acceptRequest(String friendID, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.acceptRequest(friendID, callback);
    }

    public static void rejectRequest(String friendID, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.rejectRequest(friendID, callback);
    }

    public static void removeFriend(String friendID, FriendsDAO.AddFriendCallback callback) {
        friendsDAO.removeFriend(friendID, callback);
    }

    public static void getUIDFriendsList(String userID, FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.getUIDFriendsList(userID, callback);
    }

    public static void getUIDFriendsList(FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.getUIDFriendsList(callback);
    }
}
