package ru.practicum.event.model;

import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserMapper;
import ru.practicum.utils.Constants;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utils.Converter.decodeAndConvertToLocalDateTime;

public class EventMapper {
    private EventMapper() {
    }

    public static Event toEvent(NewEventDto newEventDto, User initiator, Category category, Location location) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(decodeAndConvertToLocalDateTime(newEventDto.getEventDate()))
                .initiator(initiator)
                .location(location)
                .paid(newEventDto.getPaid() != null && newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration() == null || newEventDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }

    public static EventFullDto toEventFullDto(Event event) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getEventDate().format(Constants.FORMATTER),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.isPaid(),
                event.getTitle(),
                event.getCreatedOn().format(Constants.FORMATTER),
                event.getDescription(),
                LocationMapper.toLocationDto(event.getLocation()),
                event.getParticipantLimit(),
                event.getPublishedOn() == null ? null : event.getPublishedOn().format(Constants.FORMATTER),
                event.isRequestModeration(),
                event.getState()
        );
    }

    public static EventShortDto toEventShortDto(Event event) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getEventDate().format(Constants.FORMATTER),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.isPaid(),
                event.getTitle()
        );
    }

    public static List<EventShortDto> toEventShortDto(Collection<Event> events) {
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    public static List<EventFullDto> toEventFullDto(Collection<Event> events) {
        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }
}
