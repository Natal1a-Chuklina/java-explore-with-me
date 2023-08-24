package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.PublicSearchParameters;
import ru.practicum.utils.Constants;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/friends")
@Slf4j
@RequiredArgsConstructor
@Validated
public class FriendEventsPrivateController {
    private final EventService eventService;
    private static final String DEFAULT_BOOLEAN_VALUE = "false";

    @GetMapping("/{friendId}/events")
    public List<EventShortDto> getFriendEvents(@PathVariable @Positive long userId,
                                               @PathVariable @Positive long friendId,
                                               @RequestParam(required = false) String text,
                                               @RequestParam(required = false) Long[] categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false) String rangeStart,
                                               @RequestParam(required = false) String rangeEnd,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(defaultValue = DEFAULT_BOOLEAN_VALUE) boolean onlyAvailable,
                                               @RequestParam(defaultValue = Constants.DEFAULT_START_VALUE) @Min(0) int from,
                                               @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Getting events in which the user with id = {} participates by user with id = {}", friendId, userId);
        PublicSearchParameters parameters = new PublicSearchParameters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort);
        return eventService.getFriendEvents(parameters, userId, friendId, from, size);
    }

    @GetMapping("/events")
    public List<EventShortDto> getFriendEvents(@PathVariable @Positive long userId,
                                               @RequestParam(required = false) String text,
                                               @RequestParam(required = false) Long[] categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false) String rangeStart,
                                               @RequestParam(required = false) String rangeEnd,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(defaultValue = DEFAULT_BOOLEAN_VALUE) boolean onlyFriends,
                                               @RequestParam(defaultValue = DEFAULT_BOOLEAN_VALUE) boolean onlyAvailable,
                                               @RequestParam(defaultValue = Constants.DEFAULT_START_VALUE) @Min(0) int from,
                                               @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        if (onlyFriends) {
            log.info("Getting events in which friends of user with id = {} participate", userId);
        } else {
            log.info("Getting events in which friends and users who are followed by user with id = {} participate", userId);
        }
        PublicSearchParameters parameters = new PublicSearchParameters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort);
        return eventService.getFriendEvents(parameters, userId, from, size, onlyFriends);
    }
}
