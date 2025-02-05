package com.peppe289.echotrail.exceptions;

public class UserCollectionException extends RuntimeException {
    public UserCollectionException(String message) {
        super(message);
    }

    public UserCollectionException() {
        super();
    }
}
