package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserShortDto;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class EventFullDto extends EventDto {
    private String createdOn;
    private String description;
    private LocationDto location;
    private int participantLimit;
    private String publishedOn;
    private boolean requestModeration;
    private EventState state;

    public EventFullDto(long id, String annotation, CategoryDto category, String eventDate, UserShortDto initiator,
                        boolean paid, String title, String createdOn, String description, LocationDto location,
                        int participantLimit, String publishedOn, boolean requestModeration, EventState state) {
        super(id, annotation, category, eventDate, initiator, paid, title);
        this.createdOn = createdOn;
        this.description = description;
        this.location = location;
        this.participantLimit = participantLimit;
        this.publishedOn = publishedOn;
        this.requestModeration = requestModeration;
        this.state = state;
    }
}
