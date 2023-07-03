package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.AdminSearchParameters;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.utils.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventAdminController {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @Positive long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest updateEventUserRequest) {
        log.info("Updating event with id = {} by admin", eventId);
        return eventService.updateEventByAdmin(eventId, updateEventUserRequest);
    }

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) Long[] users,
                                        @RequestParam(required = false) String[] states,
                                        @RequestParam(required = false) Long[] categories,
                                        @RequestParam(required = false) String rangeStart,
                                        @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(defaultValue = Constants.DEFAULT_START_VALUE) @Min(0) int from,
                                        @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) @Positive int size) {
        log.info("Getting {} events from {} event by admin", size, from);
        AdminSearchParameters parameters = new AdminSearchParameters(users, states, categories, rangeStart, rangeEnd);
        return eventService.getEvents(parameters, from, size);
    }
}
