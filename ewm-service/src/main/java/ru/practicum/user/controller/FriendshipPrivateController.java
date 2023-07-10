package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.FriendshipService;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.FollowerType;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class FriendshipPrivateController {
    private final FriendshipService friendshipService;

    @PostMapping("/friends/{friendId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFriend(@PathVariable @Positive long userId, @PathVariable @Positive long friendId) {
        log.info("Adding user with id = {} as friend of user with id = {}", friendId, userId);
        friendshipService.addFriend(userId, friendId);
    }

    @DeleteMapping("/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFriend(@PathVariable @Positive long userId, @PathVariable @Positive long friendId) {
        log.info("Removing user with id = {} from friends of user with id = {}", friendId, userId);
        friendshipService.deleteFriend(userId, friendId);
    }

    @PatchMapping("/friends/{friendId}/cancel")
    public void cancelFriendshipRequest(@PathVariable @Positive long userId, @PathVariable @Positive long friendId) {
        log.info("Canceling friendship request from user with id = {} to user with id = {}", friendId, userId);
        friendshipService.cancelFriendshipRequest(userId, friendId);
    }

    @DeleteMapping("/followers/{followerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFollower(@PathVariable @Positive long userId, @PathVariable @Positive long followerId) {
        log.info("Removing user with id = {} from followers of user with id = {}", followerId, userId);
        friendshipService.deleteFollower(userId, followerId);
    }

    @GetMapping("/friends/requests")
    public List<UserDto> gettingFriendshipRequests(@PathVariable @Positive long userId) {
        log.info("Getting friendship requests to user with id = {}", userId);
        return friendshipService.getFriendshipRequests(userId);
    }

    @GetMapping("/friends")
    public List<UserDto> getFriends(@PathVariable @Positive long userId) {
        log.info("Getting friends of user with id = {}", userId);
        return friendshipService.getFriends(userId);
    }

    @GetMapping("/followers")
    public List<UserDto> getFollowers(@PathVariable @Positive long userId, @RequestParam FollowerType type) {
        if (type.equals(FollowerType.FOLLOWED)) {
            log.info("Getting users who followed user with id = {}", userId);
        } else {
            log.info("Getting users who user with id = {} is following", userId);
        }
        return friendshipService.getFollowers(userId, type);
    }
}
