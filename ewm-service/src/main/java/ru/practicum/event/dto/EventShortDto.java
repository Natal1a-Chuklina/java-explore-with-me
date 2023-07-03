package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
public class EventShortDto extends EventDto {
    public EventShortDto(long id, String annotation, CategoryDto category, String eventDate, UserShortDto initiator,
                         boolean paid, String title) {
        super(id, annotation, category, eventDate, initiator, paid, title);
    }
}
