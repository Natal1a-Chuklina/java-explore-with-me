package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.LocationDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.UserStateAction;
import ru.practicum.utils.Constants;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPrivateController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventPrivateControllerTest {
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;
    @MockBean
    private EventService eventService;
    private static final String annotation = RandomStringUtils.random(25, true, false);
    private static final Long category = 1L;
    private static final String description = RandomStringUtils.random(25, true, false);
    private static final String eventDate = "2023-09-09 14:00:00";
    private static final LocationDto location = new LocationDto(55.7f, 37.6f);
    private static final boolean paid = false;
    private static final int participantLimit = 20;
    private static final boolean requestModeration = false;
    private static final String title = RandomStringUtils.random(5, true, false);

    @SneakyThrows
    @Test
    void createEvent_WhenNoInputBody_ThenReturnBadRequest() {
        assertThat(mockMvc.perform(post("/users/{userId}/events", 1))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(eventService);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidNewEventDtosStream")
    void createEvent_WhenInputBodyNotValid_ThenReturnBadRequest(NewEventDto input) {
        assertThat(mockMvc.perform(post("/users/{userId}/events", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(eventService);
    }

    static Stream<NewEventDto> notValidNewEventDtosStream() {
        NewEventDto nullAnnotation = new NewEventDto(null, category, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto blankAnnotation = new NewEventDto("   ", category, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto shortAnnotation = new NewEventDto("short", category, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto longAnnotation = new NewEventDto(RandomStringUtils.random(2001, true, false),
                category, description, eventDate, location, paid, participantLimit, requestModeration, title);
        NewEventDto nullCategory = new NewEventDto(annotation, null, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto zeroCategory = new NewEventDto(annotation, 0L, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto negativeCategory = new NewEventDto(annotation, 0L, description, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto nullDescription = new NewEventDto(annotation, category, null, eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto blankDescription = new NewEventDto(annotation, category, "  ", eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto shortDescription = new NewEventDto(annotation, category, "short", eventDate, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto longDescription = new NewEventDto(annotation, category, RandomStringUtils.random(7001,
                true, false), eventDate, location, paid, participantLimit, requestModeration, title);
        NewEventDto nullEventDate = new NewEventDto(annotation, category, description, null, location, paid,
                participantLimit, requestModeration, title);
        NewEventDto blankEventDate = new NewEventDto(annotation, category, description, "   ", location, paid,
                participantLimit, requestModeration, title);
        NewEventDto nullLocation = new NewEventDto(annotation, category, description, eventDate, null, paid,
                participantLimit, requestModeration, title);
        NewEventDto emptyLocation = new NewEventDto(annotation, category, description, eventDate, new LocationDto(), paid,
                participantLimit, requestModeration, title);
        NewEventDto emptyLatLocation = new NewEventDto(annotation, category, description, eventDate,
                new LocationDto(null, 5.4F), paid, participantLimit, requestModeration, title);
        NewEventDto emptyLonLocation = new NewEventDto(annotation, category, description, eventDate,
                new LocationDto(5.4F, null), paid, participantLimit, requestModeration, title);
        NewEventDto nullTitle = new NewEventDto(annotation, category, description, eventDate, location, paid,
                participantLimit, requestModeration, null);
        NewEventDto blankTitle = new NewEventDto(annotation, category, description, eventDate, location, paid,
                participantLimit, requestModeration, "   ");
        NewEventDto shortTitle = new NewEventDto(annotation, category, description, eventDate, location, paid,
                participantLimit, requestModeration, "sh");
        NewEventDto longTitle = new NewEventDto(annotation, category, description, eventDate, location, paid,
                participantLimit, requestModeration, RandomStringUtils.random(121, true, false));
        NewEventDto negativeParticipantLimit = new NewEventDto(annotation, category, description, eventDate, location,
                paid, -10, requestModeration, title);

        return Stream.of(nullAnnotation, blankAnnotation, shortAnnotation, longAnnotation, nullCategory, zeroCategory,
                negativeCategory, nullDescription, blankDescription, shortDescription, longDescription, nullEventDate,
                blankEventDate, nullLocation, emptyLocation, emptyLatLocation, emptyLonLocation, nullTitle, blankTitle,
                shortTitle, longTitle, negativeParticipantLimit);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidPathVariablesStream")
    void createEvent_WhenPathVariableNotValid_ThenReturnBadRequest(Object pathVariable) {
        NewEventDto input = new NewEventDto(annotation, category, description, eventDate, location, paid,
                participantLimit, requestModeration, title);

        assertThat(mockMvc.perform(post("/users/{userId}/events", pathVariable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(eventService);
    }

    static Stream<Object> notValidPathVariablesStream() {
        return Stream.of(-1, 0, "wrong_value");
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("notValidUpdateEventUserRequestDtosStream")
    void updateEvent_WhenInputBodyNotValid_ThenReturnBadRequest(UpdateEventUserRequest input) {
        assertThat(mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString())
                .contains(Constants.INCORRECTLY_MADE_REQUEST_MESSAGE);

        Mockito.verifyNoInteractions(eventService);
    }

    static Stream<UpdateEventUserRequest> notValidUpdateEventUserRequestDtosStream() {
        UserStateAction stateAction = UserStateAction.SEND_TO_REVIEW;

        UpdateEventUserRequest shortAnnotation = new UpdateEventUserRequest("short", category, description, eventDate,
                location, paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest longAnnotation = new UpdateEventUserRequest(RandomStringUtils.random(2001,
                true, false), category, description, eventDate, location, paid, participantLimit,
                requestModeration, stateAction, title);
        UpdateEventUserRequest negativeCategory = new UpdateEventUserRequest(annotation, -1L, description,
                eventDate, location, paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest zeroCategory = new UpdateEventUserRequest(annotation, 0L, description,
                eventDate, location, paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest shortDescription = new UpdateEventUserRequest(annotation, category, "short",
                eventDate, location, paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest longDescription = new UpdateEventUserRequest(annotation, category,
                RandomStringUtils.random(7001, true, false), eventDate, location, paid,
                participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest emptyLocation = new UpdateEventUserRequest(annotation, category, description, eventDate,
                new LocationDto(), paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest emptyLatLocation = new UpdateEventUserRequest(annotation, category, description, eventDate,
                new LocationDto(null, 3.4f), paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest emptyLonLocation = new UpdateEventUserRequest(annotation, category, description, eventDate,
                new LocationDto(3.4f, null), paid, participantLimit, requestModeration, stateAction, title);
        UpdateEventUserRequest negativeParticipantLimit = new UpdateEventUserRequest(annotation, category, description,
                eventDate, location, paid, -1, requestModeration, stateAction, title);
        UpdateEventUserRequest shortTitle = new UpdateEventUserRequest(annotation, category, description, eventDate,
                location, paid, participantLimit, requestModeration, stateAction, "sh");
        UpdateEventUserRequest longTitle = new UpdateEventUserRequest(annotation, category, description, eventDate,
                location, paid, participantLimit, requestModeration, stateAction, RandomStringUtils.random(121,
                true, false));

        return Stream.of(shortAnnotation, longAnnotation, negativeCategory, zeroCategory, shortDescription,
                longDescription, emptyLocation, emptyLatLocation, emptyLonLocation, negativeParticipantLimit, shortTitle,
                longTitle);
    }
}