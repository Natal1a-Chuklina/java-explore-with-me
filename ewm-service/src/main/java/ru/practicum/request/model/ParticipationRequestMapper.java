package ru.practicum.request.model;

import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.model.User;
import ru.practicum.utils.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ParticipationRequestMapper {
    private ParticipationRequestMapper() {
    }

    public static ParticipationRequestDto toRequestDto(ParticipationRequest participationRequest) {
        return new ParticipationRequestDto(
                participationRequest.getId(),
                participationRequest.getCreated().format(Constants.FORMATTER),
                participationRequest.getEvent().getId(),
                participationRequest.getRequester().getId(),
                participationRequest.getStatus(),
                participationRequest.getVisibility()
        );
    }

    public static ParticipationRequest toRequest(User requester, Event event, RequestStatus status,
                                                 VisibilityType visibility) {
        return new ParticipationRequest(
                requester,
                event,
                LocalDateTime.now(),
                status,
                visibility);
    }

    public static List<ParticipationRequestDto> toRequestDto(List<ParticipationRequest> requests) {
        return requests.stream().map(ParticipationRequestMapper::toRequestDto).collect(Collectors.toList());
    }
}
