package ru.practicum.event;

import ru.practicum.event.dto.*;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(long userId, NewEventDto newEventDto);

    EventFullDto getEventById(long userId, long eventId);

    List<EventShortDto> getUserEvents(long userId, int from, int size);

    EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest newEventDto);

    EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest newEventDto);

    List<EventFullDto> getEvents(AdminSearchParameters parameters, int from, int size);

    List<EventShortDto> getEvents(HttpServletRequest request, PublicSearchParameters parameters, int from, int size);

    EventFullDto getEventById(long eventId, HttpServletRequest request);

    List<ParticipationRequestDto> getEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest);
}
