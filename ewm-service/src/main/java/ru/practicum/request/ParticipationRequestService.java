package ru.practicum.request;

import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.VisibilityType;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(long userId, long eventId, VisibilityType visibility);

    List<ParticipationRequestDto> getRequestsByUserId(long userId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
