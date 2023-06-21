package ru.practicum.stats;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.EndpointHitMapper;
import ru.practicum.stats.model.QEndpointHit;
import ru.practicum.utils.Constants;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsStorage statsStorage;

    @Override
    public void saveEndpointHit(InputEndpointHit inputEndpointHit) {
        EndpointHit endpointHit = statsStorage.save(EndpointHitMapper.toEndpointHit(inputEndpointHit));
        log.info("Saved endpoint's request info with id {}", endpointHit.getId());
    }

    @Override
    public List<EndpointStats> getStats(String start, String end, String[] uris, boolean unique) {
        LocalDateTime from = decodeAndParseDate(start);
        LocalDateTime to = decodeAndParseDate(end);
        BooleanExpression predicate = QEndpointHit.endpointHit.timeStamp.between(from, to);

        if (uris != null && uris.length != 0) {
            predicate = predicate.and(QEndpointHit.endpointHit.uri.in(uris));
        }

        Iterable<EndpointHit> allItems = statsStorage.findAll(predicate);

        List<EndpointStats> stats = (unique) ? calcStatsWithoutRepeatings(allItems) : calcStatsWithRepeatings(allItems);
        log.info("Got statistics for {} endpoints", stats.size());
        return stats;
    }

    private List<EndpointStats> calcStatsWithRepeatings(Iterable<EndpointHit> endpointHits) {
        List<EndpointStats> endpointStatsList = new ArrayList<>();
        Map<EndpointStats, Integer> stats = new HashMap<>();

        for (EndpointHit endpointHit : endpointHits) {
            EndpointStats endpointStats = EndpointHitMapper.toEndpointStats(endpointHit);
            stats.put(endpointStats, stats.getOrDefault(endpointStats, 0) + 1);
        }

        for (Map.Entry<EndpointStats, Integer> entry : stats.entrySet()) {
            entry.getKey().setHits(entry.getValue());
            endpointStatsList.add(entry.getKey());
        }

        return sortByHits(endpointStatsList);
    }

    private List<EndpointStats> calcStatsWithoutRepeatings(Iterable<EndpointHit> endpointHits) {
        List<EndpointStats> endpointStatsList = new ArrayList<>();
        Map<EndpointStats, Set<String>> stats = new HashMap<>();

        for (EndpointHit endpointHit : endpointHits) {
            EndpointStats endpointStats = EndpointHitMapper.toEndpointStats(endpointHit);
            stats.computeIfAbsent(endpointStats, k -> new HashSet<>());
            stats.get(endpointStats).add(endpointHit.getIp());
        }

        for (Map.Entry<EndpointStats, Set<String>> entry : stats.entrySet()) {
            entry.getKey().setHits(entry.getValue().size());
            endpointStatsList.add(entry.getKey());
        }

        return sortByHits(endpointStatsList);
    }

    private List<EndpointStats> sortByHits(List<EndpointStats> notSortedList) {
        return notSortedList.stream()
                .sorted(Comparator.comparingInt(EndpointStats::getHits).reversed())
                .collect(Collectors.toList());
    }

    private LocalDateTime decodeAndParseDate(String date) {
        String decodedDate = URLDecoder.decode(date, StandardCharsets.UTF_8);
        return LocalDateTime.parse(decodedDate, Constants.FORMATTER);
    }
}
