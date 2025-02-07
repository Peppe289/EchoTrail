package com.peppe289.echotrail.model;

import org.jetbrains.annotations.NotNull;

public class Friend {
    private String name;
    private boolean onPendingRequest;
    private boolean isFriend;
    private String uid;

    public Friend() {
    }

    public Friend(String name, boolean onPendingRequest, boolean isFriend, String uid) {
        this.name = name;
        this.onPendingRequest = onPendingRequest;
        this.isFriend = isFriend;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnPendingRequest() {
        return onPendingRequest;
    }

    public void setOnPendingRequest(boolean onPendingRequest) {
        this.onPendingRequest = onPendingRequest;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
