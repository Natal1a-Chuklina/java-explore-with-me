package ru.practicum.event.model;

import ru.practicum.event.dto.LocationDto;

public class LocationMapper {
    private LocationMapper() {
    }

    public static Location toLocation(LocationDto locationDto) {
        return new Location(locationDto.getLat(), locationDto.getLon());
    }

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
