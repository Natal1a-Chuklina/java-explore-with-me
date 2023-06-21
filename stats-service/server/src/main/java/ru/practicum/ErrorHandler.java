package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.utils.ApiError;
import ru.practicum.utils.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimeParseException(DateTimeParseException e) {
        log.warn("Date time parse exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(), LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(), LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestValueException(MissingRequestValueException e) {
        log.warn("Validation exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(), LocalDateTime.now().format(Constants.FORMATTER));
    }

    private List<String> mapStackTrace(StackTraceElement[] stackTraceElements) {
        return Arrays.stream(stackTraceElements).map(StackTraceElement::toString).collect(Collectors.toList());
    }
}
