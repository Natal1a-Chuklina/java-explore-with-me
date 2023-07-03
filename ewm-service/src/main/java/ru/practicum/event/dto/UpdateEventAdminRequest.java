package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.event.model.AdminStateAction;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class UpdateEventAdminRequest extends UpdateEventRequest {
    private AdminStateAction stateAction;

    public UpdateEventAdminRequest(String annotation, Long category, String description, String eventDate,
                                   LocationDto location, Boolean paid, Integer participantLimit,
                                   Boolean requestModeration, String title, AdminStateAction stateAction) {
        super(annotation, category, description, eventDate, location, paid, participantLimit, requestModeration, title);
        this.stateAction = stateAction;
    }
}
