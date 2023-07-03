package ru.practicum.request;

import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(long userId, long eventId);

    List<ParticipationRequestDto> getRequestsByUserId(long userId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);
}
