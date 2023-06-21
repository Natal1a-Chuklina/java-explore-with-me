package ru.practicum.stats.model;

import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;
import ru.practicum.utils.Constants;

import java.time.LocalDateTime;

public class EndpointHitMapper {
    private EndpointHitMapper() {
    }

    public static EndpointHit toEndpointHit(InputEndpointHit inputEndpointHit) {
        return new EndpointHit(
                null,
                inputEndpointHit.getApp(),
                inputEndpointHit.getUri(),
                inputEndpointHit.getIp(),
                LocalDateTime.parse(inputEndpointHit.getTimestamp(), Constants.FORMATTER)
        );
    }

    public static EndpointStats toEndpointStats(EndpointHit endpointHit) {
        return new EndpointStats(
                endpointHit.getApp(),
                endpointHit.getUri(),
                0
        );
    }

}
