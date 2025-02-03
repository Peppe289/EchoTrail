package com.peppe289.echotrail.exceptions;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(String message) {
        super(message);
    }

    public FriendNotFoundException() {
        super();
    }
}
