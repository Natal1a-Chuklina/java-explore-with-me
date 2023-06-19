package ru.practicum.stats;

import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;

import java.util.List;

public interface StatsService {
    void saveEndpointHit(InputEndpointHit inputEndpointHit);

    List<EndpointStats> getStats(String start, String end, String[] uris, boolean unique);
}
