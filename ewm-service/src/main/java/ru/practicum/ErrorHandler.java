package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.DataModificationProhibitedException;
import ru.practicum.utils.ApiError;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
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
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Method argument not valid exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatchException(TypeMismatchException e) {
        log.warn("Type mismatch exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        log.warn("Validation exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageConversionException(HttpMessageConversionException e) {
        log.warn("Http message conversion exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestValueException(MissingRequestValueException e) {
        log.warn("Missing request value exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.CONFLICT,
                Constants.DATA_INTEGRITY_VIOLATION_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataModificationProhibitedException(DataModificationProhibitedException e) {
        log.warn("Data modification prohibited exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.CONFLICT,
                Constants.OPERATION_CONDITIONS_NOT_MET_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleSecurityException(SecurityException e) {
        log.warn("Security exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.CONFLICT,
                Constants.OPERATION_CONDITIONS_NOT_MET_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(EntityNotFoundException e) {
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.NOT_FOUND, Constants.NOT_FOUND_MESSAGE,
                e.getMessage(), LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ApiError handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("Http method not supported exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.NOT_IMPLEMENTED,
                Constants.METHOD_NOT_SUPPORTED_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(Throwable e) {
        log.error("Unknown error happened: {}", e.getMessage(), e);
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR,
                Constants.UNKNOWN_ERROR_MESSAGE, e.getMessage(), LocalDateTime.now().format(Constants.FORMATTER));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimeParseException(DateTimeParseException e) {
        log.warn("Date time parse exception: {}", e.getMessage());
        return new ApiError(mapStackTrace(e.getStackTrace()), HttpStatus.BAD_REQUEST,
                Constants.INCORRECTLY_MADE_REQUEST_MESSAGE, e.getMessage(),
                LocalDateTime.now().format(Constants.FORMATTER));
    }

    private List<String> mapStackTrace(StackTraceElement[] stackTraceElements) {
        return Arrays.stream(stackTraceElements).map(StackTraceElement::toString).collect(Collectors.toList());
    }
}
