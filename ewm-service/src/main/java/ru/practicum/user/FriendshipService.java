package ru.practicum.user;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.FollowerType;

import java.util.List;

public interface FriendshipService {
    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    void cancelFriendshipRequest(long userId, long friendId);

    void deleteFollower(long userId, long followerId);

    List<UserDto> getFriendshipRequests(long userId);

    List<UserDto> getFriends(long userId);

    List<UserDto> getFollowers(long userId, FollowerType followerType);
}
