package ru.practicum.exception;

public class DataModificationProhibitedException extends RuntimeException {
    public DataModificationProhibitedException() {
    }

    public DataModificationProhibitedException(String message) {
        super(message);
    }

    public DataModificationProhibitedException(String message, Throwable cause) {
        super(message, cause);
    }
}
