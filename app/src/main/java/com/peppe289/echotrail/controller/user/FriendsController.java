package com.peppe289.echotrail.controller.user;

import android.content.Context;
import com.peppe289.echotrail.dao.user.FriendsDAO;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.FriendNotFoundException;
import com.peppe289.echotrail.model.FriendItem;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.utils.ControllerCallback;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FriendsController {
    private static FriendsDAO friendsDAO;
    private static UserDAO userDAO;

    public static void init() {
        friendsDAO = new FriendsDAO();
        userDAO = new UserDAO();
    }

    public static void init(FriendsDAO friendsDAO) {
        FriendsController.friendsDAO = friendsDAO;
    }


    public interface FriendsCallback {
        void onPreExecute();
        void onSuccess(List<FriendItem> friends);
        void onError(String errorMessage);
    }

    public static void loadFriends(Context context, FriendsCallback callback) {
        callback.onPreExecute();

        friendsDAO.searchPendingRequests(new ControllerCallback<List<String>, Exception> () {
            @Override
            public void onSuccess(List<String> pendingFriends) {
                List<FriendItem> friendItems = new ArrayList<>();

                if (pendingFriends != null) {
                    for (String id : pendingFriends) {
                        String finalId = id.trim();
                        userDAO.getUserInfo(finalId, new ControllerCallback<User, Exception>() {
                            @Override
                            public void onSuccess(User userData) {
                                friendItems.add(new FriendItem(userData.getUsername(), true, false, finalId));
                                callback.onSuccess(friendItems);
                            }

                            @Override
                            public void onError(Exception error) {
                                // TODO: handle error.
                            }
                        });
                    }
                }

                // Recupero lista amici
                FriendsController.getUIDFriendsList(new FriendsDAO.GetFriendsCallback() {
                    @Override
                    public void onFriendsRetrieved(List<String> friends) {
                        if (friends != null) {
                            for (String id : friends) {
                                String finalId = id.trim();
                                userDAO.getUserInfo(finalId, new ControllerCallback<User, Exception>() {
                                    @Override
                                    public void onSuccess(User userData) {
                                        friendItems.add(new FriendItem(userData.getUsername(), false, true, finalId));
                                        callback.onSuccess(friendItems);
                                    }

                                    @Override
                                    public void onError(Exception error) {

                                    }
                                });
                            }
                        }
                        callback.onSuccess(friendItems);
                    }

                    @Override
                    public void onError(ErrorType error) {
                        callback.onError(error.getMessage(context));
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.GET_PENDING_REQUESTS_ERROR.getMessage(context));
            }
        });
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

        FriendsController.getUIDFriendsList(new FriendsDAO.GetFriendsCallback() {
            @Override
            public void onFriendsRetrieved(List<String> friends) {
                if (friends != null) {
                    for (String friend : friends) {
                        if (friend.trim().compareTo(friendID.trim()) == 0) {
                            friendsDAO.removeFriend(friendID, new ControllerCallback<String, Exception>() {
                                @Override
                                public void onSuccess(String result) {
                                    // at last, the first request work.
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
                    }
                }
            }

            @Override
            public void onError(ErrorType error) {

            }
        });
    }

    public static void getUIDFriendsList(FriendsDAO.GetFriendsCallback callback) {
        friendsDAO.synchronizeFriendsList(new ControllerCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                friendsDAO.getUIDFriendsList(UserController.getUid(), callback);
            }

            @Override
            public void onError(Exception error) {
                if (error instanceof FriendNotFoundException) {
                    callback.onError(ErrorType.GET_FRIENDS_ERROR);
                } else {
                    callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            }
        });
    }
}
