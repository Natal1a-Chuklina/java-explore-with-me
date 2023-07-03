package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.PublicSearchParameters;
import ru.practicum.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPublicController {
    private final EventService eventService;
    private static final String DEFAULT_AVAILABLE_VALUE = "false";

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable @Positive long eventId, HttpServletRequest request) {
        log.info("Getting event by id = {}", eventId);
        return eventService.getEventById(eventId, request);
    }

    @GetMapping
    public List<EventShortDto> getEvents(HttpServletRequest request,
                                         @RequestParam(required = false) String text,
                                         @RequestParam(required = false) Long[] categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = DEFAULT_AVAILABLE_VALUE) boolean onlyAvailable,
                                         @RequestParam(defaultValue = Constants.DEFAULT_START_VALUE) @Min(0) int from,
                                         @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Getting {} events from {} event", size, from);
        PublicSearchParameters parameters = new PublicSearchParameters(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort);
        return eventService.getEvents(request, parameters, from, size);
    }
}
