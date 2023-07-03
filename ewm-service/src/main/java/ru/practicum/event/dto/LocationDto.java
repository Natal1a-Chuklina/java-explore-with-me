package ru.practicum.event.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}
