package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventStorage;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.exception.DataModificationProhibitedException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestMapper;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.VisibilityType;
import ru.practicum.user.UserStorage;
import ru.practicum.user.model.User;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final EventStorage eventStorage;
    private final ParticipationRequestStorage requestStorage;
    private final UserStorage userStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ParticipationRequestDto createRequest(long userId, long eventId, VisibilityType visibility) {
        checkUserExistence(userId);
        Event event = getEvent(eventId);
        User user = userStorage.getReferenceById(userId);

        if (event.getInitiator().getId() == userId) {
            log.warn("Attempt to create participation request by initiator with id = {} in event with id = {}", userId,
                    eventId);
            throw new DataModificationProhibitedException(Constants.INITIATOR_CANNOT_CREATE_REQUEST_MESSAGE);
        }

        checkEventStateAndDate(event, userId);
        checkEventAvailable(event);
        ParticipationRequest request = requestStorage.save(ParticipationRequestMapper.toRequest(user, event,
                getRequestStatus(event), visibility));

        log.info("Created participation request with id = {}", request.getId());
        return ParticipationRequestMapper.toRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(long userId) {
        checkUserExistence(userId);
        List<ParticipationRequest> requests = requestStorage.findByRequester_Id(userId);
        log.info("Received {} participation requests", requests.size());
        return ParticipationRequestMapper.toRequestDto(requests);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ParticipationRequest request = getRequestByRequestIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);

        requestStorage.save(request);
        log.info("Request with id = {} was canceled by user with id = {}", requestId, userId);
        return ParticipationRequestMapper.toRequestDto(request);
    }

    private RequestStatus getRequestStatus(Event event) {
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            return RequestStatus.CONFIRMED;
        } else {
            return RequestStatus.PENDING;
        }
    }

    private void checkEventStateAndDate(Event event, long userId) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Attempt to create participation request in not published event with id = {} by user with id = {}",
                    event.getId(), userId);
            throw new DataModificationProhibitedException(String.format(Constants.CANNOT_REQUEST_NOT_PUBLISHED_EVENT_MESSAGE,
                    event.getState()));
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            log.warn("Attempt to create event participation request after event start");
            throw new DataModificationProhibitedException(Constants.CANNOT_CREATE_REQUEST_IN_ALREADY_STARTED_EVENT_MESSAGE);
        }
    }

    private ParticipationRequest getRequestByRequestIdAndRequesterId(long requestId, long userId) {
        Optional<ParticipationRequest> request = requestStorage.findByIdAndRequester_Id(requestId, userId);

        if (request.isEmpty()) {
            log.warn("Attempt to get nonexistent participation request with id = {} by user with id = {}", request,
                    userId);
            throw new EntityNotFoundException(String.format(Constants.USER_REQUEST_NOT_FOUND_MESSAGE, userId, requestId));
        }

        return request.get();
    }

    private void checkEventAvailable(Event event) {
        int confirmedRequests = requestStorage.countByEvent_IdAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequests) {
            log.warn("Attempt to create participation request in not available event");
            throw new DataModificationProhibitedException(String.format(Constants.EVENT_NOT_AVAILABLE_MESSAGE, event.getId()));
        }
    }

    private Event getEvent(long eventId) {
        Optional<Event> event = eventStorage.findById(eventId);

        if (event.isEmpty()) {
            log.warn("Attempt to get nonexistent event with id = {}", eventId);
            throw new EntityNotFoundException(String.format(Constants.EVENT_NOT_FOUND_MESSAGE, eventId));
        }

        return event.get();
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Attempt to get nonexistent user with id = {}", userId);
            throw new EntityNotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }
}
