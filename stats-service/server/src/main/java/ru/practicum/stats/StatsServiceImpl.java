package ru.practicum.stats;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.util.ArrayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.model.EndpointHitMapper;
import ru.practicum.stats.model.QEndpointHit;
import ru.practicum.utils.Constants;

import javax.validation.ValidationException;
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
        checkSearchInterval(from, to);
        BooleanExpression timestampBetweenPredicate = QEndpointHit.endpointHit.timeStamp.between(from, to);
        Set<EndpointHit> endpointHits = new HashSet<>();

        if (!ArrayUtils.isEmpty(uris)) {
            for (String uri : uris) {
                BooleanExpression uriStartsWithPredicate = QEndpointHit.endpointHit.uri.startsWith(uri);
                statsStorage.findAll(timestampBetweenPredicate.and(uriStartsWithPredicate)).forEach(endpointHits::add);
            }
        } else {
            statsStorage.findAll(timestampBetweenPredicate).forEach(endpointHits::add);
        }

        List<EndpointStats> stats = (unique) ? calcStatsWithoutRepeatings(endpointHits) : calcStatsWithRepeatings(endpointHits);
        log.info("Got statistics for {} endpoints", stats.size());
        return stats;
    }

    private List<EndpointStats> calcStatsWithRepeatings(Set<EndpointHit> endpointHits) {
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

    private List<EndpointStats> calcStatsWithoutRepeatings(Set<EndpointHit> endpointHits) {
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

    private void checkSearchInterval(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            log.warn("Attempt to get statistics from {} to {}", start.format(Constants.FORMATTER),
                    end.format(Constants.FORMATTER));
            throw new ValidationException(Constants.START_SHOULD_BE_BEFORE_END_MESSAGE);
        }
    }
}
