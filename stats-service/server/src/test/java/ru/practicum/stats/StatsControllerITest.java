package ru.practicum.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsControllerITest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private StatsService statsService;

    @SneakyThrows
    @Test
    void saveEndpointRequest_WhenRequestBodyItValid_ThenReturnCreated() {
        InputEndpointHit input = new InputEndpointHit("ewm-main-service", "/events/1", "192.163.0.1",
                "2022-09-06 11:00:23");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated());

        verify(statsService, Mockito.times(1)).saveEndpointHit(input);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("wrongInputEndpointRequestStream")
    void saveEndpointRequest_WhenRequestBodyItNotValid_ThenReturnBadRequest(InputEndpointHit input) {
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(statsService);
    }

    @SneakyThrows
    @Test
    void saveEndpointRequest_WhenServiceThrowsDateTimeParseException_ThenReturnBadRequest() {
        InputEndpointHit input = new InputEndpointHit("ewm-main-service", "/events/1", "192.163.0.1",
                "2022-09-06 11:00:23");
        doThrow(DateTimeParseException.class).when(statsService).saveEndpointHit(any(InputEndpointHit.class));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());

        verify(statsService, Mockito.times(1)).saveEndpointHit(any(InputEndpointHit.class));
    }

    static Stream<InputEndpointHit> wrongInputEndpointRequestStream() {
        InputEndpointHit nullApp = new InputEndpointHit(null, "/events/1", "192.163.0.1",
                "2022-09-06 11:00:23");
        InputEndpointHit blankApp = new InputEndpointHit("  ", "/events/1", "192.163.0.1",
                "2022-09-06 11:00:23");
        InputEndpointHit nullUri = new InputEndpointHit("ewm-main-service", null, "192.163.0.1",
                "2022-09-06 11:00:23");
        InputEndpointHit blankUri = new InputEndpointHit("ewm-main-service", "  ", "192.163.0.1",
                "2022-09-06 11:00:23");
        InputEndpointHit nullIp = new InputEndpointHit("ewm-main-service", "/events/1", null,
                "2022-09-06 11:00:23");
        InputEndpointHit blankIp = new InputEndpointHit("ewm-main-service", "/events/1", "  ",
                "2022-09-06 11:00:23");
        InputEndpointHit nullTimestamp = new InputEndpointHit("ewm-main-service", "/events/1", "192.163.0.1",
                null);
        InputEndpointHit blankTimestamp = new InputEndpointHit("ewm-main-service", "/events/1", "192.163.0.1",
                "  ");

        return Stream.of(nullApp, blankApp, nullUri, blankUri, nullIp, blankIp, nullTimestamp, blankTimestamp);
    }

    @SneakyThrows
    @Test
    void getStats_WhenAllParametersExist_ThenReturnOk() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";
        String[] uris = {"/events/1", "/events/2"};
        boolean unique = true;
        List<EndpointStats> stats = List.of(new EndpointStats("ewm-main-service", "/events/1", 4),
                new EndpointStats("ewm-main-service", "/events/2", 2));
        when(statsService.getStats(start, end, uris, unique)).thenReturn(stats);

        String output = mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", uris)
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(output)
                .as("Check return value when getting endpoint's statistics")
                .isNotNull()
                .isEqualTo(objectMapper.writeValueAsString(stats));
        verify(statsService, Mockito.times(1)).getStats(start, end, uris, unique);
    }

    @SneakyThrows
    @Test
    void getStats_WhenNotAllParametersExist_ThenReturnOk() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isOk());

        verify(statsService, Mockito.times(1)).getStats(start, end, null, false);
    }

    @SneakyThrows
    @Test
    void getStats_ThrowsDateTimeParseException_ThenReturnBadRequest() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";
        when(statsService.getStats(anyString(), anyString(), any(), anyBoolean()))
                .thenThrow(DateTimeParseException.class);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isBadRequest());

        verify(statsService, Mockito.times(1)).getStats(anyString(), anyString(), any(),
                anyBoolean());
    }

    @SneakyThrows
    @Test
    void getStats_WhenParametersDoNotExist_ThenReturnBadRequest() {
        mockMvc.perform(get("/stats")
                        .param("start", "2022-09-06 11:00:23")
                        .param("unique", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/stats")
                        .param("end", "2022-09-06 11:00:23")
                        .param("unique", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/stats")
                        .param("unique", "true"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(statsService);
    }
}