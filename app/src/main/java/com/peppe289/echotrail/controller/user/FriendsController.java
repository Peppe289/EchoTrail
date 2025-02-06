package com.peppe289.echotrail.controller.user;

import com.peppe289.echotrail.annotations.TestOnly;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.dao.user.FriendsDAO;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.FriendNotFoundException;
import com.peppe289.echotrail.model.FriendItem;
import com.peppe289.echotrail.model.User;
import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.FriendsCallback;
import com.peppe289.echotrail.utils.ErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FriendsController {
    private static FriendsDAO friendsDAO;
    private static UserDAO userDAO;

    public static void init() {
        friendsDAO = FriendsDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    @TestOnly
    public static void init(FriendsDAO friendsDAO) {
        FriendsController.friendsDAO = friendsDAO;
    }

    public static void loadFriends(ControllerCallback<List<FriendItem>, ErrorType> callback) {
        friendsDAO.searchPendingRequests(new FriendsCallback<List<String>, Exception> () {
            @Override
            public void onSuccess(List<String> pendingFriends) {
                List<FriendItem> friendItems = new ArrayList<>();

                if (pendingFriends != null) {
                    for (String id : pendingFriends) {
                        String finalId = id.trim();
                        userDAO.getUserInfo(finalId, new UserCallback<User, Exception>() {
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
                FriendsController.getUIDFriendsList(new ControllerCallback<List<String>, ErrorType>() {
                    @Override
                    public void onSuccess(List<String> friends) {
                        if (friends != null) {
                            for (String id : friends) {
                                String finalId = id.trim();
                                userDAO.getUserInfo(finalId, new UserCallback<User, Exception>() {
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
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                callback.onError(ErrorType.GET_PENDING_REQUESTS_ERROR);
            }
        });
    }

    public static void requestToBeFriends(String friendId, ControllerCallback<Void, ErrorType> callback) {
        //friendsDAO.requestToBeFriends(friendId, callback);
        friendsDAO.requestToBeFriends(friendId, new FriendsCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                if (error instanceof FriendNotFoundException) {
                    callback.onError(ErrorType.FRIEND_NOT_FOUND_ERROR);
                } else {
                    callback.onError(ErrorType.SEND_FRIEND_REQUEST_ERROR);
                }
            }
        });
    }

    public static void acceptRequest(String friendID, ControllerCallback<Void, ErrorType> callback) {
        friendsDAO.acceptRequest(friendID, new FriendsCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                if (error instanceof FriendNotFoundException) {
                    callback.onError(ErrorType.FRIEND_NOT_FOUND_ERROR);
                } else {
                    callback.onError(ErrorType.UNKNOWN_ERROR);
                }
            }
        });
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
    public static void removeFriend(String friendID, ControllerCallback<Void, ErrorType> callback) {
        AtomicBoolean atLastOne = new AtomicBoolean(false);

        FriendsCallback<String, Exception> friendsCallback = new FriendsCallback<String, Exception>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Exception error) {
                if (atLastOne.get()) {
                    callback.onSuccess(null);
                } else {
                    if (error instanceof FriendNotFoundException) {
                        callback.onError(ErrorType.FRIEND_NOT_FOUND_ERROR);
                    } else {
                        callback.onError(ErrorType.REMOVE_FRIEND_ERROR);
                    }
                }
            }
        };

        FriendsController.getUIDFriendsList(new ControllerCallback<List<String>, ErrorType>() {
            @Override
            public void onSuccess(List<String> friends) {
                if (friends != null) {
                    for (String friend : friends) {
                        if (friend.trim().compareTo(friendID.trim()) == 0) {
                            friendsDAO.removeFriend(friendID, new FriendsCallback<String, Exception>() {
                                @Override
                                public void onSuccess(String result) {
                                    // at last, the first request work.
                                    atLastOne.set(true);
                                    friendsDAO.rejectRequest(friendID, friendsCallback);
                                }

                                @Override
                                public void onError(Exception error) {
                                    // also if removeFriend fail, I run anyway rejectRequest
                                    friendsDAO.rejectRequest(friendID, friendsCallback);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onError(ErrorType error) {
                ///  TODO: handle error
            }
        });
    }

    public static void getUIDFriendsList(ControllerCallback<List<String>, ErrorType> callback) {
        friendsDAO.synchronizeFriendsList(new FriendsCallback<Void, Exception>() {
            @Override
            public void onSuccess(Void result) {
                friendsDAO.getUIDFriendsList(UserController.getUid(), new FriendsCallback<List<String>, Exception>() {
                    @Override
                    public void onSuccess(List<String> result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onError(Exception error) {
                        callback.onError(ErrorType.GET_FRIENDS_ERROR);
                    }
                });
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
