package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.exception.DataRecordException;
import ru.practicum.exception.DataRetrievalException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate restTemplate;
    private static final String SAVE_ENDPOINT_PATH = "/hit";
    private static final String GET_STATS_PATH_WITH_URIS = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
    private static final String GET_STATS_PATH_WITHOUT_URIS = "/stats?start={start}&end={end}&unique={unique}";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        restTemplate = builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public void saveEndpointRequest(InputEndpointHit inputEndpointHit) {
        HttpEntity<InputEndpointHit> requestEntity = new HttpEntity<>(inputEndpointHit, defaultHeaders());

        try {
            restTemplate.exchange(SAVE_ENDPOINT_PATH, HttpMethod.POST, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            throw new DataRecordException(String.format("An error occurred while saving endpoint request stats. Status " +
                    "code: %s. Error message: %s.", e.getStatusCode(), e.getMessage()));
        }
    }

    public List<EndpointStats> getStatistics(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        ResponseEntity<EndpointStats[]> responseEntity;
        try {
            if (uris == null || uris.length == 0) {
                responseEntity = restTemplate.getForEntity(GET_STATS_PATH_WITHOUT_URIS, EndpointStats[].class,
                        parameters(start, end, uris, unique));
            } else {
                responseEntity = restTemplate.getForEntity(GET_STATS_PATH_WITH_URIS, EndpointStats[].class,
                        parameters(start, end, uris, unique));
            }
        } catch (HttpStatusCodeException e) {
            throw new DataRetrievalException(String.format("An error occurred while getting statistics. Status " +
                    "code: %s. Error message: %s.", e.getStatusCode(), e.getMessage()));
        }

        return List.of(responseEntity.getBody());
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private Map<String, Object> parameters(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8));
        parameters.put("end", URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8));

        if (uris != null && uris.length != 0) {
            parameters.put("uris", uris);
        }

        parameters.put("unique", unique);

        return parameters;
    }
}
