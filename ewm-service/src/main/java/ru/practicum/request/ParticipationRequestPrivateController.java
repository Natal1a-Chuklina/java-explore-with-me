package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.VisibilityType;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ParticipationRequestPrivateController {
    private final ParticipationRequestService requestService;
    private static final String DEFAULT_VISIBILITY = "FOLLOWERS";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable @Positive long userId,
                                                 @RequestParam @Positive long eventId,
                                                 @RequestParam(defaultValue = DEFAULT_VISIBILITY) VisibilityType visibility) {
        log.info("Creating participation request in event with id = {} by user with id {}", eventId, userId);
        return requestService.createRequest(userId, eventId, visibility);
    }

    @GetMapping
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable @Positive long userId) {
        log.info("Getting participation requests of user with id = {}", userId);
        return requestService.getRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive long userId,
                                                 @PathVariable @Positive long requestId) {
        log.info("Canceling participation request with id = {} by user with id = {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }
}
