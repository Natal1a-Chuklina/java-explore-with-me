package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;
    private static final String DEFAULT_UNIQUE_VALUE = "false";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveEndpointRequest(@Valid @RequestBody InputEndpointHit inputEndpointHit) {
        log.info("Saving endpoint's request info: {}", inputEndpointHit);
        statsService.saveEndpointHit(inputEndpointHit);
    }

    @GetMapping("/stats")
    public List<EndpointStats> getStats(@RequestParam @NotBlank String start, @NotBlank @RequestParam String end,
                                        @RequestParam(required = false) String[] uris,
                                        @RequestParam(defaultValue = DEFAULT_UNIQUE_VALUE) boolean unique) {
        log.info("Getting statistics from {} to {}", start, end);
        return statsService.getStats(start, end, uris, unique);
    }
}
