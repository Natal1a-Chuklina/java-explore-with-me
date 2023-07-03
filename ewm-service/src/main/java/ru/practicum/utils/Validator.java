package ru.practicum.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import java.time.LocalDateTime;

@Slf4j
public class Validator {
    private Validator() {
    }

    public static void validateStartAndEndDates(String startDate, String endDate) {
        if (StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate)) {
            return;
        }

        LocalDateTime start = Converter.decodeAndConvertToLocalDateTime(startDate);
        LocalDateTime end = Converter.decodeAndConvertToLocalDateTime(endDate);

        if (!end.isAfter(start)) {
            throw new ValidationException(String.format(Constants.END_SHOULD_BE_AFTER_START_MESSAGE,
                    start.format(Constants.FORMATTER), end.format(Constants.FORMATTER)));
        }
    }
}
