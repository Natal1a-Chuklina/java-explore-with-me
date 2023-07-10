package ru.practicum.request.dto;

import lombok.*;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.model.VisibilityType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ParticipationRequestDto {
    private long id;
    private String created;
    private long event;
    private long requester;
    private RequestStatus status;
    private VisibilityType visibility;
}
