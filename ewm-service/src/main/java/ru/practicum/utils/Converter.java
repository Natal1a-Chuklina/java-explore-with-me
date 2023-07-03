package ru.practicum.utils;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.event.dto.SortType;
import ru.practicum.event.model.EventState;

import javax.validation.ValidationException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Converter {
    private Converter() {
    }

    public static LocalDateTime decodeAndConvertToLocalDateTime(String date) {
        String decodedDate = URLDecoder.decode(date, StandardCharsets.UTF_8);
        return LocalDateTime.parse(decodedDate, Constants.FORMATTER);
    }

    public static List<EventState> convertStringToEventState(String[] states) {
        return Arrays.stream(states).map(Converter::convertStringToEventState).collect(Collectors.toList());
    }

    public static SortType convertStringToSortType(String sortType) {
        try {
            return SortType.valueOf(sortType);
        } catch (IllegalArgumentException e) {
            log.warn("Attempt to use nonexistent sort type: {}", sortType);
            throw new ValidationException(String.format(Constants.SORT_TYPE_DOES_NOT_EXIST_MESSAGE, sortType),
                    e.getCause());
        }
    }

    public static EventState convertStringToEventState(String eventState) {
        try {
            return EventState.valueOf(eventState);
        } catch (IllegalArgumentException e) {
            log.warn("Attempt to use nonexistent event state: {}", eventState);
            throw new ValidationException(String.format(Constants.EVENT_STATE_DOES_NOT_EXIST_MESSAGE, eventState),
                    e.getCause());
        }
    }
}
