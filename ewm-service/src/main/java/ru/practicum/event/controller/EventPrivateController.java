package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.utils.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Creating new event: {}", newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable @Positive long userId, @PathVariable @Positive long eventId) {
        log.info("Getting event with id = {} by user with id = {}", eventId, userId);
        return eventService.getEventById(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable @Positive long userId,
                                             @RequestParam(defaultValue = Constants.DEFAULT_START_VALUE) @Min(0) int from,
                                             @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Getting {} events from {} event of user with id = {}", size, from, userId);
        return eventService.getUserEvents(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive long userId, @PathVariable @Positive long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Updating event with id = {} by user with id = {}", eventId, userId);
        return eventService.updateEventByUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable @Positive long userId,
                                                          @PathVariable @Positive long eventId) {
        log.info("Getting participation requests in event with id = {} by event initiator with id = {}", eventId, userId);
        return eventService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Positive long userId,
                                                              @PathVariable @Positive long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        log.info("Updating event participation request statuses for {} requests in event with id = {} by " +
                "initiator with id = {}", updateRequest.getRequestIds().size(), eventId, userId);
        return eventService.updateRequestStatus(userId, eventId, updateRequest);
    }
}
