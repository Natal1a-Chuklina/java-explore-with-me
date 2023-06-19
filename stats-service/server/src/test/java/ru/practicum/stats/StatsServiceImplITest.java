package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.EndpointStats;
import ru.practicum.InputEndpointHit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class StatsServiceImplITest {
    private final StatsService statsService;

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStats_WhenNoDataInDB_ThenReturnEmptyList() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";

        assertThatCode(() -> assertThat(statsService.getStats(start, end, null, false))
                .as("Check return value when the database is empty")
                .isNotNull()
                .asList()
                .isEmpty()).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStats_WhenUrisDoNotExist_ThenReturnAllDataBetweenStartAndEnd() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";
        String app1 = "ewm-main-service_1";
        String app2 = "ewm-main-service_2";
        String uri1 = "/events/1";
        String uri2 = "/events/2";
        String ip1 = "192.163.0.1";
        String ip2 = "192.163.0.2";
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri1, ip1, "2023-09-06 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app2, uri1, ip2, "2021-09-16 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri2, ip2, "2021-09-16 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri2, ip1, "2021-09-26 11:00:23"));

        assertThatCode(() -> assertThat(statsService.getStats(start, end, null, false))
                .as("Check return value when the database is empty")
                .isNotNull()
                .asList()
                .hasSize(2)
                .contains(new EndpointStats(app1, uri2, 2), Index.atIndex(0))
                .contains(new EndpointStats(app2, uri1, 1), Index.atIndex(1))).doesNotThrowAnyException();

        assertThatCode(() -> assertThat(statsService.getStats("2022-09-06 11:00:23", "2023-09-16 11:00:23", new String[]{},
                false))
                .as("Check return value when the database is not empty and there are no uris for search")
                .isNotNull()
                .asList()
                .hasSize(1)
                .contains(new EndpointStats(app1, uri1, 1))).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getStats_WhenUrisExist_ThenReturnAllDataBetweenStartAndEnd() {
        String start = "2021-09-06 11:00:23";
        String end = "2022-09-06 11:00:23";
        String app1 = "ewm-main-service_1";
        String app2 = "ewm-main-service_2";
        String uri1 = "/events/1";
        String uri2 = "/events/2";
        String ip1 = "192.163.0.1";
        String ip2 = "192.163.0.2";
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri1, ip1, "2023-09-06 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app2, uri1, ip2, "2021-09-16 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri2, ip2, "2021-09-16 11:00:23"));
        statsService.saveEndpointHit(new InputEndpointHit(app1, uri2, ip1, "2021-09-26 11:00:23"));

        assertThatCode(() -> assertThat(statsService.getStats(start, end, new String[]{uri1}, false))
                .as("Check return value when the database is not empty and there are uris for search")
                .isNotNull()
                .asList()
                .hasSize(1)
                .contains(new EndpointStats(app2, uri1, 1))).doesNotThrowAnyException();
    }
}