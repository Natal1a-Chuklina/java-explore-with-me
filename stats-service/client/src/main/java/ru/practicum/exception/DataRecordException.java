package ru.practicum.exception;

public class DataRecordException extends RuntimeException {
    public DataRecordException() {
    }

    public DataRecordException(String message) {
        super(message);
    }

    public DataRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
