package ru.practicum.event;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;
import ru.practicum.StatsClient;
import ru.practicum.category.CategoryStorage;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.*;
import ru.practicum.exception.DataModificationProhibitedException;
import ru.practicum.request.ParticipationRequestStorage;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestMapper;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.UserStorage;
import ru.practicum.user.model.User;
import ru.practicum.utils.Constants;
import ru.practicum.utils.Validator;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.querydsl.core.util.ArrayUtils.isEmpty;
import static io.micrometer.core.instrument.util.StringUtils.isBlank;
import static ru.practicum.utils.Converter.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final LocationStorage locationStorage;
    private final ParticipationRequestStorage requestStorage;
    private final StatsClient statsClient;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto createEvent(long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate());

        User initiator = getUser(userId);
        Category category = getCategory(newEventDto.getCategory());
        Location location = locationStorage.save(LocationMapper.toLocation(newEventDto.getLocation()));
        Event event = eventStorage.save(EventMapper.toEvent(newEventDto, initiator, category, location));

        log.info("Created event with id = {}", event.getId());
        return composeEventFullDto(event);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto getEventById(long userId, long eventId) {
        Event event = getEventByEventIdAndUserId(eventId, userId);
        log.info("Received event with id = {} by initiator with id = {}", eventId, userId);
        return composeEventFullDto(event);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<EventShortDto> getUserEvents(long userId, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);

        List<Event> events = eventStorage.findByInitiator_Id(userId, page).getContent();
        List<EventShortDto> eventDtos = EventMapper.toEventShortDto(events);
        fillEventDtoList(eventDtos, getEarliestPublishDate(events));

        log.info("Received {} events of user with id = {}", events.size(), userId);
        return sortEvents(eventDtos, SortType.VIEWS);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest newEventDto) {
        Event event = getEventByEventIdAndUserId(eventId, userId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            log.warn("Attempt to update already published event with id = {} by user with id = {}", eventId, userId);
            throw new DataModificationProhibitedException(Constants.PUBLISHED_EVENT_UPDATE_PROHIBITED_MESSAGE);
        }

        updateEventByNotNullFields(newEventDto, event);

        if (newEventDto.getStateAction() != null) {
            if (newEventDto.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        eventStorage.save(event);
        log.info("Updated event with id = {} by user with id = {}", event, userId);
        return composeEventFullDto(event);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest newEventDto) {
        Event event = getEventByEventId(eventId, false);
        updateEventByNotNullFields(newEventDto, event);

        if (newEventDto.getStateAction() != null) {
            if (newEventDto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                setPublishedOnDate(event);
                setEventState(event, EventState.PUBLISHED);
            } else {
                setEventState(event, EventState.CANCELED);
            }
        }

        eventStorage.save(event);
        log.info("Updated event with id = {} by admin", eventId);
        return composeEventFullDto(event);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<EventFullDto> getEvents(AdminSearchParameters parameters, int from, int size) {
        Validator.validateStartAndEndDates(parameters.getRangeStart(), parameters.getRangeEnd());
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        BooleanBuilder builder = new BooleanBuilder();
        composeSearchPredicate(builder, parameters);
        List<Event> events;

        if (builder.getValue() == null) {
            events = eventStorage.findAll(page).getContent();
        } else {
            events = eventStorage.findAll(builder.getValue(), page).getContent();
        }

        List<EventFullDto> eventFullDtos = EventMapper.toEventFullDto(events);
        fillEventDtoList(eventFullDtos, getEarliestPublishDate(events));
        log.info("Received {} events by admin", events.size());
        return eventFullDtos;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventFullDto getEventById(long eventId, HttpServletRequest request) {
        saveStatistics(request);
        Event event = getEventByEventId(eventId, true);
        log.info("Received event with id = {}", eventId);
        return composeEventFullDto(event);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<EventShortDto> getEvents(HttpServletRequest request, PublicSearchParameters parameters, int from, int size) {
        saveStatistics(request);
        Validator.validateStartAndEndDates(parameters.getRangeStart(), parameters.getRangeEnd());
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        BooleanBuilder builder = new BooleanBuilder();
        composeSearchPredicate(builder, parameters);

        List<Event> events = eventStorage.findAll(builder.getValue(), page).getContent();
        List<EventShortDto> eventShortDtos = EventMapper.toEventShortDto(events);
        fillEventDtoList(eventShortDtos, getEarliestPublishDate(events));

        if (parameters.isOnlyAvailable()) {
            filterNotAvailableEvents(events, eventShortDtos);
        }

        log.info("Received {} events", eventShortDtos.size());
        if (parameters.getSort() == null) {
            return eventShortDtos;
        } else {
            return sortEvents(eventShortDtos, convertStringToSortType(parameters.getSort()));
        }
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId) {
        if (!eventStorage.existsByIdAndInitiator_Id(eventId, userId)) {
            log.warn("Attempt to get participation requests in event with id = {} by not event initiator with id = {}",
                    eventId, userId);
            throw new SecurityException(Constants.NOT_INITIATOR_CANNOT_GET_EVENT_REQUESTS_MESSAGE);
        }

        List<ParticipationRequest> requests = requestStorage.findByEvent_Id(eventId);
        log.info("Received {} participation requests in event with id = {}", requests.size(), eventId);
        return ParticipationRequestMapper.toRequestDto(requests);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public EventRequestStatusUpdateResult updateRequestStatus(long userId, long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        int eventParticipantLimit = getEventParticipantLimit(eventId, userId);
        int confirmedRequests = getConfirmedRequests(eventId);
        boolean needToRejectNotConfirmedRequests = needToRejectConfirmedRequests(confirmedRequests,
                eventParticipantLimit, updateRequest, eventId);

        List<ParticipationRequest> requests = findRequestsToUpdate(eventId, updateRequest.getRequestIds());
        checkRequestModerationPossibilities(requests);
        updateRequests(requests, updateRequest.getStatus());

        if (needToRejectNotConfirmedRequests) {
            updateRequests(requestStorage.findByEvent_IdAndStatus(eventId, RequestStatus.PENDING), RequestStatus.REJECTED);
        }

        log.info("Updated {} event participation requests with status {}", requests.size(), updateRequest.getStatus());
        return composeEventRequestStatusUpdateResult(eventId);
    }

    private boolean needToRejectConfirmedRequests(int confirmedRequests, int eventParticipantLimit,
                                                  EventRequestStatusUpdateRequest updateRequest, long eventId) {
        boolean needToRejectNotConfirmedRequests = false;

        if (eventParticipantLimit <= confirmedRequests) {
            log.warn("Attempt to moderate event participation requests when event reached participant limit.");
            throw new DataModificationProhibitedException(String.format(Constants.EVENT_REACHED_PARTICIPANT_LIMIT_MESSAGE,
                    eventId));
        }

        if (updateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            int confirmedRequestsAfterUpdate = confirmedRequests + updateRequest.getRequestIds().size();

            if (eventParticipantLimit < confirmedRequestsAfterUpdate) {
                log.warn("Attempt to confirm more requests than available to confirm");
                throw new DataModificationProhibitedException(Constants.EVENT_WILL_REACH_PARTICIPANT_LIMIT_MESSAGE);
            } else if (eventParticipantLimit == confirmedRequestsAfterUpdate) {
                needToRejectNotConfirmedRequests = true;
            }
        }

        return needToRejectNotConfirmedRequests;
    }

    private int getEventParticipantLimit(long eventId, long userId) {
        Event event = getEventByEventIdAndUserId(eventId, userId);

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            log.warn("Attempt to moderate event participation requests in event that doesn't request moderation id = {}",
                    eventId);
            throw new DataModificationProhibitedException(String.format(Constants.EVENT_DOESNT_NEED_REQUEST_MODERATION_MESSAGE,
                    eventId));
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            log.warn("Attempt to moderate event participation requests after event start");
            throw new DataModificationProhibitedException(Constants.CANNOT_MODERATE_REQUEST_IN_ALREADY_STARTED_EVENT_MESSAGE);
        }

        return event.getParticipantLimit();
    }

    private List<ParticipationRequest> findRequestsToUpdate(long eventId, Set<Long> requestIds) {
        List<ParticipationRequest> requests = requestStorage.findByIdInAndEvent_Id(requestIds, eventId);

        if (requests.size() < requestIds.size()) {
            log.warn("Attempt to moderate nonexistent event participation request");
            throw new EntityNotFoundException(Constants.NOT_ALL_REQUESTS_FOUND_MESSAGE);
        }

        return requests;
    }

    private void checkRequestModerationPossibilities(List<ParticipationRequest> requests) {
        for (ParticipationRequest request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                log.warn("Attempt to moderate participation request not in the right status: expected status - {}, " +
                        "actual status - {}", RequestStatus.PENDING, request.getStatus());
                throw new DataModificationProhibitedException(
                        String.format(Constants.CANNOT_MODERATE_NOT_PENDING_REQUEST_MESSAGE, request.getStatus()));
            }
        }
    }

    private void updateRequests(List<ParticipationRequest> requests, RequestStatus status) {
        if (status.equals(RequestStatus.CONFIRMED) || status.equals(RequestStatus.REJECTED)) {
            requests.forEach(request -> request.setStatus(status));
        } else {
            log.warn("Attempt to moderate participation requests with not available status: {}", status);
            throw new DataModificationProhibitedException(String.format(
                    Constants.CANNOT_MODERATE_REQUEST_WITH_SUCH_STATUS_MESSAGE, status));
        }

        requestStorage.saveAll(requests);
        log.info("Updated {} requests with status {}", requests.size(), status);
    }

    private EventRequestStatusUpdateResult composeEventRequestStatusUpdateResult(long eventId) {
        List<ParticipationRequest> confirmed = requestStorage.findByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        List<ParticipationRequest> rejected = requestStorage.findByEvent_IdAndStatus(eventId, RequestStatus.REJECTED);

        return new EventRequestStatusUpdateResult(ParticipationRequestMapper.toRequestDto(confirmed),
                ParticipationRequestMapper.toRequestDto(rejected));
    }

    private void filterNotAvailableEvents(List<Event> events, List<EventShortDto> eventShortDtos) {
        List<EventShortDto> eventsToRemove = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            EventShortDto eventDto = eventShortDtos.get(i);

            if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= eventDto.getConfirmedRequests()) {
                eventsToRemove.add(eventDto);
            }
        }

        log.info("Removed {} not available events", eventsToRemove.size());
        eventShortDtos.removeAll(eventsToRemove);
    }

    private void saveStatistics(HttpServletRequest request) {
        InputEndpointHit inputEndpointHit = new InputEndpointHit(Constants.APP_NAME, request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(Constants.FORMATTER));
        statsClient.saveEndpointRequest(inputEndpointHit);
        log.info("Statistics saved");
    }

    private void composeSearchPredicate(BooleanBuilder builder, AdminSearchParameters parameters) {
        if (!isEmpty(parameters.getUsers())) {
            builder.and(QEvent.event.initiator.id.in(parameters.getUsers()));
        }

        if (!isEmpty(parameters.getCategories())) {
            builder.and(QEvent.event.category.id.in(parameters.getCategories()));
        }

        if (!isBlank(parameters.getRangeStart())) {
            builder.and(QEvent.event.eventDate.after(decodeAndConvertToLocalDateTime(parameters.getRangeStart())));
        }

        if (!isBlank(parameters.getRangeEnd())) {
            builder.and(QEvent.event.eventDate.before(decodeAndConvertToLocalDateTime(parameters.getRangeEnd())));
        }

        if (!isEmpty(parameters.getStates())) {
            builder.and(QEvent.event.state.in(convertStringToEventState(parameters.getStates())));
        }
    }

    private void composeSearchPredicate(BooleanBuilder builder, PublicSearchParameters parameters) {
        builder.and(QEvent.event.state.eq(EventState.PUBLISHED));

        if (!isBlank(parameters.getText())) {
            builder.and(QEvent.event.annotation.containsIgnoreCase(parameters.getText())
                    .or(QEvent.event.description.containsIgnoreCase(parameters.getText())));
        }

        if (!isEmpty(parameters.getCategories())) {
            builder.and(QEvent.event.category.id.in(parameters.getCategories()));
        }

        if (parameters.getPaid() != null) {
            builder.and(QEvent.event.paid.eq(parameters.getPaid()));
        }

        if (isBlank(parameters.getRangeStart()) && isBlank(parameters.getRangeEnd())) {
            builder.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }

        if (!isBlank(parameters.getRangeStart())) {
            builder.and(QEvent.event.eventDate.after(decodeAndConvertToLocalDateTime(parameters.getRangeStart())));
        }

        if (!isBlank(parameters.getRangeEnd())) {
            builder.and(QEvent.event.eventDate.before(decodeAndConvertToLocalDateTime(parameters.getRangeEnd())));
        }
    }

    private EventFullDto composeEventFullDto(Event event) {
        EventFullDto eventDto = EventMapper.toEventFullDto(event);

        if (event.getState().equals(EventState.PUBLISHED)) {
            eventDto.setConfirmedRequests(getConfirmedRequests(event.getId()));
            eventDto.setViews(getViews(event.getId(), event.getPublishedOn()));
        }

        return eventDto;
    }

    private void fillEventDtoList(List<? extends EventDto> eventDtoList, Optional<LocalDateTime> earliestPublishDate) {
        if (earliestPublishDate.isEmpty()) {
            return;
        }

        Map<Long, EventDto> eventDtos = eventDtoList.stream()
                .collect(Collectors.toMap(EventDto::getId, Function.identity()));
        List<ParticipationRequest> requests = requestStorage.findByEvent_IdInAndStatus(eventDtos.keySet(),
                RequestStatus.CONFIRMED);

        for (ParticipationRequest request : requests) {
            EventDto eventDto = eventDtos.get(request.getEvent().getId());
            eventDto.setConfirmedRequests(eventDto.getConfirmedRequests() + 1);
        }

        Map<String, Long> uris = new HashMap<>();
        eventDtos.keySet().forEach(id -> uris.put(String.format(Constants.EVENT_ENDPOINT, id), id));

        List<EndpointStats> stats = statsClient.getStatistics(earliestPublishDate.get(), LocalDateTime.now(),
                new ArrayList<>(uris.keySet()), true);

        for (EndpointStats statistics : stats) {
            if (statistics.getApp().equals(Constants.APP_NAME) && uris.containsKey(statistics.getUri())) {
                long eventId = uris.get(statistics.getUri());
                eventDtos.get(eventId).setViews(statistics.getHits());
            }
        }
    }

    private Optional<LocalDateTime> getEarliestPublishDate(List<Event> events) {
        return events.stream()
                .filter(event -> event.getState().equals(EventState.PUBLISHED))
                .map(Event::getPublishedOn)
                .min(Comparator.naturalOrder());
    }

    private void setEventState(Event event, EventState newState) {
        if (!event.getState().equals(EventState.PENDING)) {
            if (newState.equals(EventState.PUBLISHED)) {
                log.warn("Attempt to publish event with not right state: expected state - {}, actual state - {}",
                        EventState.PENDING, event.getState());
                throw new DataModificationProhibitedException(String.format(
                        Constants.CANNOT_PUBLISH_NOT_PENDING_EVENT_MESSAGE, event.getState()));
            } else {
                log.warn("Attempt to cancel event with not right state: expected state - {}, actual state - {}",
                        EventState.PENDING, event.getState());
                throw new DataModificationProhibitedException(String.format(
                        Constants.CANNOT_CANCEL_NOT_PENDING_EVENT_MESSAGE, event.getState()));
            }
        }

        event.setState(newState);
    }

    private void updateEventByNotNullFields(UpdateEventRequest newEventDto, Event event) {
        if (!isBlank(newEventDto.getAnnotation())) {
            event.setAnnotation(newEventDto.getAnnotation());
        }

        if (newEventDto.getCategory() != null) {
            event.setCategory(getCategory(newEventDto.getCategory()));
        }

        if (!isBlank(newEventDto.getDescription())) {
            event.setDescription(newEventDto.getDescription());
        }

        if (!isBlank(newEventDto.getEventDate())) {
            event.setEventDate(validateEventDate(newEventDto.getEventDate()));
        }

        if (newEventDto.getLocation() != null) {
            long locationId = event.getLocation().getId();
            Location newLocation = locationStorage.save(LocationMapper.toLocation(newEventDto.getLocation()));
            event.setLocation(newLocation);
            locationStorage.deleteById(locationId);
        }

        if (newEventDto.getPaid() != null) {
            event.setPaid(newEventDto.getPaid());
        }

        if (newEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        }

        if (newEventDto.getRequestModeration() != null) {
            event.setRequestModeration(newEventDto.getRequestModeration());
        }

        if (!isBlank(newEventDto.getTitle())) {
            event.setTitle(newEventDto.getTitle());
        }
    }

    private void setPublishedOnDate(Event event) {
        LocalDateTime now = LocalDateTime.now();

        if (event.getEventDate().isBefore(now.plusHours(1))) {
            log.warn("Attempt to publish event with start date less than an hour away");
            throw new DataModificationProhibitedException(Constants.EVENT_SHOULD_BE_LATER_THAN_ONE_HOUR_MESSAGE);
        }

        event.setPublishedOn(now);
    }

    private LocalDateTime validateEventDate(String eventDate) {
        LocalDateTime date = decodeAndConvertToLocalDateTime(eventDate);
        LocalDateTime earliestDate = LocalDateTime.now().plusHours(2);

        if (date.isBefore(earliestDate)) {
            throw new ValidationException(String.format(Constants.EVENT_DATE_SHOULD_BE_IN_FUTURE_MESSAGE,
                    earliestDate.format(Constants.FORMATTER)));
        }

        return date;
    }

    private int getConfirmedRequests(long eventId) {
        int confirmedRequests = requestStorage.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        log.info("Found {} confirmed requests to event with id = {}", confirmedRequests, eventId);
        return confirmedRequests;
    }

    private <T extends EventDto> List<T> sortEvents(List<T> unsortedList, SortType sortType) {
        switch (sortType) {
            case VIEWS:
                return unsortedList.stream()
                        .sorted(Comparator.comparingInt(EventDto::getViews).reversed())
                        .collect(Collectors.toList());
            case EVENT_DATE:
                return unsortedList.stream()
                        .sorted(Comparator.comparing(EventDto::getEventDate))
                        .collect(Collectors.toList());
            default:
                log.warn("Attempt to sort events by nonexistent sort type: {}", sortType);
                throw new IllegalArgumentException(String.format(Constants.SORT_TYPE_DOES_NOT_EXIST_MESSAGE, sortType));
        }
    }

    private int getViews(long eventId, LocalDateTime publishedOn) {
        List<String> uris = List.of(String.format(Constants.EVENT_ENDPOINT, eventId));
        List<EndpointStats> stats = statsClient.getStatistics(publishedOn, LocalDateTime.now(), uris, true);
        int views = 0;

        for (EndpointStats statistics : stats) {
            if (statistics.getApp().equals(Constants.APP_NAME)) {
                views = statistics.getHits();
                break;
            }
        }

        log.info("Found {} views of event with id = {}", views, eventId);
        return views;
    }

    private Event getEventByEventIdAndUserId(long eventId, long userId) {
        Optional<Event> event = eventStorage.findByIdAndInitiator_Id(eventId, userId);

        if (event.isEmpty()) {
            log.warn("Attempt to get event with id = {} that user with id = {} doesn't have", eventId, userId);
            throw new EntityNotFoundException(String.format(Constants.USER_EVENT_NOT_FOUND_MESSAGE, userId, eventId));
        }

        return event.get();
    }

    private Event getEventByEventId(long eventId, boolean requiredPublished) {
        Optional<Event> event;
        if (requiredPublished) {
            event = eventStorage.findByIdAndState(eventId, EventState.PUBLISHED);
        } else {
            event = eventStorage.findById(eventId);
        }

        if (event.isEmpty()) {
            log.warn("Attempt to get event with id = {} that doesn't exist", eventId);
            throw new EntityNotFoundException(String.format(Constants.EVENT_NOT_FOUND_MESSAGE, eventId));
        }

        return event.get();
    }

    private Category getCategory(long catId) {
        Optional<Category> category = categoryStorage.findById(catId);

        if (category.isEmpty()) {
            log.warn("Attempt to get nonexistent category with id = {}", catId);
            throw new EntityNotFoundException(String.format(Constants.CATEGORY_NOT_FOUND_MESSAGE, catId));
        }

        return category.get();
    }

    private User getUser(long userId) {
        Optional<User> user = userStorage.findById(userId);

        if (user.isEmpty()) {
            log.warn("Attempt to get nonexistent user with id = {}", userId);
            throw new EntityNotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        return user.get();
    }
}
