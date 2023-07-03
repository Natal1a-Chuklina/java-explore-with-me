package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.event.model.UserStateAction;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class UpdateEventUserRequest extends UpdateEventRequest {
    private UserStateAction stateAction;

    public UpdateEventUserRequest(String annotation, Long category, String description, String eventDate,
                                  LocationDto location, Boolean paid, Integer participantLimit, Boolean requestModeration,
                                  UserStateAction stateAction, String title) {
        super(annotation, category, description, eventDate, location, paid, participantLimit, requestModeration, title);
        this.stateAction = stateAction;
    }
}
