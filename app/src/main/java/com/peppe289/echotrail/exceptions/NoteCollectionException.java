package com.peppe289.echotrail.exceptions;

public class NoteCollectionException extends RuntimeException {
    public NoteCollectionException(String message) {
        super(message);
    }

    public NoteCollectionException() {
        super();
    }
}
