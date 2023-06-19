package ru.practicum.stats;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {
    @Mock
    private StatsStorage statsStorage;
    @InjectMocks
    private StatsServiceImpl statsService;
    @Captor
    private ArgumentCaptor<EndpointHit> argumentCaptor;

    @Test
    void saveEndpointHit_WhenTimestampIsIncorrect_ThenThrowsDateTimeParseException() {
        InputEndpointHit wrongDate = new InputEndpointHit("ewm-main-service", "/events/1", "192.163.0.1",
                "20222-09-06 11:00:23");

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> statsService.saveEndpointHit(wrongDate));

        verifyNoInteractions(statsStorage);
    }

    @Test
    void saveEndpointHit_WhenTimeIsCorrect_ThenEndpointHitSaved() {
        String app = "ewm-main-service";
        String uri = "/events/1";
        String ip = "192.163.0.1";
        LocalDateTime timestamp = LocalDateTime.of(2022, 9, 6, 11, 0, 23);
        InputEndpointHit input = new InputEndpointHit(app, uri, ip, "2022-09-06 11:00:23");
        EndpointHit output = new EndpointHit(1L, app, uri, ip, timestamp);
        when(statsStorage.save(any(EndpointHit.class))).thenReturn(output);

        assertThatCode(() -> statsService.saveEndpointHit(input)).doesNotThrowAnyException();

        verify(statsStorage, Mockito.times(1)).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue())
                .as("Check passed argument to the endpoint's hit save method")
                .isNotNull()
                .hasFieldOrPropertyWithValue("app", app)
                .hasFieldOrPropertyWithValue("uri", uri)
                .hasFieldOrPropertyWithValue("ip", ip)
                .hasFieldOrPropertyWithValue("timeStamp", timestamp);
    }

    @Test
    void getStats_WhenStartOrEndTimeIsIncorrect_ThenThrowsDateTimeParseException() {
        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> statsService.getStats("wrong start", "2022-09-06 11:00:23", null,
                        false));

        assertThatExceptionOfType(DateTimeParseException.class)
                .isThrownBy(() -> statsService.getStats("2022-09-06 11:00:23", "wrong end", null,
                        false));

        verifyNoInteractions(statsStorage);
    }

    @Test
    void getStats_WhenRequiredUnique_ThenReturnUniqueStats() {
        String app1 = "ewm-main-service_1";
        String app2 = "ewm-main-service_2";
        String uri1 = "/events/1";
        String uri2 = "/events/2";
        String ip1 = "192.163.0.1";
        String ip2 = "192.163.0.2";
        String ip3 = "192.163.0.3";
        when(statsStorage.findAll(any(BooleanExpression.class))).thenReturn(List.of(
                new EndpointHit(1L, app1, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(2L, app1, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(3L, app1, uri1, ip2, LocalDateTime.now()),
                new EndpointHit(4L, app2, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(5L, app2, uri1, ip2, LocalDateTime.now()),
                new EndpointHit(6L, app2, uri1, ip3, LocalDateTime.now()),
                new EndpointHit(7L, app2, uri2, ip3, LocalDateTime.now())
        ));

        assertThatCode(() -> {
            List<EndpointStats> stats = statsService.getStats("2023-01-01 10:00:00", "2024-01-01 10:00:00",
                    null, true);
            assertThat(stats)
                    .as("Check the return value when ips should be unique")
                    .asList()
                    .isNotNull()
                    .hasSize(3)
                    .contains(new EndpointStats(app2, uri1, 3), Index.atIndex(0))
                    .contains(new EndpointStats(app1, uri1, 2), Index.atIndex(1))
                    .contains(new EndpointStats(app2, uri2, 1), Index.atIndex(2));
        }).doesNotThrowAnyException();

        verify(statsStorage, Mockito.times(1)).findAll(any(BooleanExpression.class));
    }

    @Test
    void getStats_WhenNotRequiredUnique_ThenReturnUniqueStats() {
        String app1 = "ewm-main-service_1";
        String app2 = "ewm-main-service_2";
        String uri1 = "/events/1";
        String uri2 = "/events/2";
        String ip1 = "192.163.0.1";
        String ip2 = "192.163.0.2";
        String ip3 = "192.163.0.3";
        when(statsStorage.findAll(any(BooleanExpression.class))).thenReturn(List.of(
                new EndpointHit(1L, app1, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(2L, app1, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(3L, app1, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(4L, app1, uri1, ip2, LocalDateTime.now()),
                new EndpointHit(5L, app2, uri1, ip1, LocalDateTime.now()),
                new EndpointHit(6L, app2, uri1, ip2, LocalDateTime.now()),
                new EndpointHit(7L, app2, uri1, ip3, LocalDateTime.now()),
                new EndpointHit(8L, app2, uri2, ip3, LocalDateTime.now())
        ));

        assertThatCode(() -> {
            List<EndpointStats> stats = statsService.getStats("2023-01-01 10:00:00", "2024-01-01 10:00:00",
                    null, false);
            assertThat(stats)
                    .as("Check return value when ips shouldn't be unique")
                    .asList()
                    .isNotNull()
                    .hasSize(3)
                    .contains(new EndpointStats(app1, uri1, 4), Index.atIndex(0))
                    .contains(new EndpointStats(app2, uri1, 3), Index.atIndex(1))
                    .contains(new EndpointStats(app2, uri2, 1), Index.atIndex(2));
        }).doesNotThrowAnyException();

        verify(statsStorage, Mockito.times(1)).findAll(any(BooleanExpression.class));
    }
}