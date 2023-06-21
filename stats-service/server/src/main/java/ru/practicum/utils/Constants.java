package ru.practicum.utils;

import java.time.format.DateTimeFormatter;

public class Constants {
    private Constants() {
    }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String INCORRECTLY_MADE_REQUEST_MESSAGE = "Incorrectly made request.";
}
