package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.DataModificationProhibitedException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.*;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {
    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void addFriend(long userId, long friendId) {
        Map<Long, Friendship> friendships = getFriendships(userId, friendId);
        List<Friendship> friendshipsToSave;

        if (friendships.isEmpty()) {
            User user = userStorage.getReferenceById(userId);
            User friend = userStorage.getReferenceById(friendId);
            friendshipsToSave = List.of(FriendshipMapper.toFriendship(user, friend, FriendshipStatus.CONFIRMED),
                    FriendshipMapper.toFriendship(friend, user, FriendshipStatus.PENDING));
        } else {
            if (!friendships.get(userId).getStatus().equals(FriendshipStatus.CONFIRMED)) {
                friendships.get(userId).setStatus(FriendshipStatus.CONFIRMED);
                friendshipsToSave = List.of(friendships.get(userId));
            } else {
                log.info("Attempt to re-add user with id = {} as friend of user with id = {}", friendId, userId);
                throw new DataModificationProhibitedException(String.format(Constants.FRIEND_REQUEST_ALREADY_SENT_MESSAGE,
                        friendId));
            }
        }

        friendshipStorage.saveAll(friendshipsToSave);
        log.info("User with id = {} added user with id = {} to friends", userId, friendId);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteFriend(long userId, long friendId) {
        Map<Long, Friendship> friendships = getFriendships(userId, friendId);

        if (friendships.isEmpty() || !friendships.get(userId).getStatus().equals(FriendshipStatus.CONFIRMED)) {
            log.warn("Attempt to delete from friends user with id = {} that does not friend of user with id = {}",
                    friendId, userId);
            throw new EntityNotFoundException(String.format(Constants.FRIEND_NOT_FOUND_MESSAGE, friendId));
        } else {
            if (!friendships.get(friendId).getStatus().equals(FriendshipStatus.CONFIRMED)) {
                friendshipStorage.deleteAll(friendships.values());
            } else {
                friendships.get(userId).setStatus(FriendshipStatus.CANCELED);
                friendshipStorage.save(friendships.get(userId));
            }
            log.info("User with id = {} removed user with id = {} from friends", userId, friendId);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelFriendshipRequest(long userId, long friendId) {
        Map<Long, Friendship> friendships = getFriendships(userId, friendId);

        if (friendships.isEmpty() || !friendships.get(userId).getStatus().equals(FriendshipStatus.PENDING)) {
            log.warn("Attempt to cancel nonexistent friendship request from user with id = {} by user with id = {}",
                    friendId, userId);
            throw new EntityNotFoundException(String.format(Constants.FRIENDSHIP_REQUEST_NOT_FOUND_MESSAGE, friendId));
        } else {
            friendships.get(userId).setStatus(FriendshipStatus.CANCELED);
            friendshipStorage.save(friendships.get(userId));
            log.info("User with id = {} canceled friendship request from user with id = {}", userId, friendId);
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteFollower(long userId, long followerId) {
        Map<Long, Friendship> friendships = getFriendships(userId, followerId);

        if (friendships.isEmpty() || friendships.get(userId).getStatus().equals(FriendshipStatus.CONFIRMED)) {
            log.warn("Attempt to remove nonexistent follower with id = {} by user with id = {}", followerId, userId);
            throw new EntityNotFoundException(String.format(Constants.FOLLOWER_NOT_FOUND_MESSAGE, followerId));
        } else {
            if (friendships.get(userId).getStatus().equals(FriendshipStatus.PENDING)) {
                log.warn("Attempt to remove from follower user with not canceled friendship request");
                throw new DataModificationProhibitedException(String.format(Constants.NEED_CANCEL_FRIENDSHIP_REQUEST_FIRST,
                        followerId));
            } else {
                friendshipStorage.deleteAll(friendships.values());
                log.info("User with id = {} removed user with id = {} from followers", userId, followerId);
            }
        }
    }

    @Override
    public List<UserDto> getFriendshipRequests(long userId) {
        checkUserExistence(userId);
        List<Friendship> friendships = friendshipStorage.findByUser_IdAndStatus(userId, FriendshipStatus.PENDING);
        List<User> requesters = friendships.stream().map(Friendship::getFriend).collect(Collectors.toList());
        log.info("Received {} friendship requests", requesters.size());
        return UserMapper.toUserDto(requesters);
    }

    @Override
    public List<UserDto> getFriends(long userId) {
        checkUserExistence(userId);
        List<User> friends = findFriends(userId);
        log.info("Received {} friends", friends.size());
        return UserMapper.toUserDto(friends);
    }

    @Override
    public List<UserDto> getFollowers(long userId, FollowerType followerType) {
        checkUserExistence(userId);
        List<User> followers = findFollowers(userId, followerType);
        log.info("Received {} followers", followers.size());
        return UserMapper.toUserDto(followers);
    }

    private List<User> findFriends(long userId) {
        Map<Long, Friendship> incoming = new HashMap<>();
        List<Friendship> outgoing = new ArrayList<>();
        fillIncomingAndOutgoingRequests(incoming, outgoing, userId);

        return outgoing.stream()
                .map(Friendship::getFriend)
                .filter(friend -> incoming.containsKey(friend.getId()))
                .collect(Collectors.toList());
    }

    private List<User> findFollowers(long userId, FollowerType followerType) {
        Map<Long, Friendship> incoming = new HashMap<>();
        List<Friendship> outgoing = new ArrayList<>();
        fillIncomingAndOutgoingRequests(incoming, outgoing, userId);

        if (followerType.equals(FollowerType.FOLLOWED)) {
            outgoing.forEach(friendship -> incoming.remove(friendship.getFriend().getId()));
            return incoming.values().stream().map(Friendship::getUser).collect(Collectors.toList());
        } else {
            return outgoing.stream().map(Friendship::getFriend)
                    .filter(user -> !incoming.containsKey(user.getId())).collect(Collectors.toList());
        }
    }

    private void fillIncomingAndOutgoingRequests(Map<Long, Friendship> incoming, List<Friendship> outgoing, long userId) {
        List<Friendship> friendships = friendshipStorage.findByUser_IdAndStatusOrFriend_IdAndStatus(userId,
                FriendshipStatus.CONFIRMED, userId, FriendshipStatus.CONFIRMED);

        for (Friendship friendship : friendships) {
            if (friendship.getUser().getId() != userId) {
                incoming.put(friendship.getUser().getId(), friendship);
            } else {
                outgoing.add(friendship);
            }
        }
    }

    private void checkUserExistence(List<Long> ids) {
        ids.forEach(this::checkUserExistence);
    }

    private void checkUserExistence(long userId) {
        if (!userStorage.existsById(userId)) {
            log.warn("Attempt to get nonexistent user with id = {}", userId);
            throw new EntityNotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private Map<Long, Friendship> getFriendships(long userId, long friendId) {
        checkIdsCorrectness(userId, friendId);
        List<Long> ids = List.of(userId, friendId);
        checkUserExistence(ids);
        return friendshipStorage.findByUser_IdInAndFriend_IdIn(ids, ids).stream()
                .collect(Collectors.toMap(friendship -> friendship.getUser().getId(), Function.identity()));
    }

    private void checkIdsCorrectness(long userId, long friendId) {
        if (userId == friendId) {
            log.warn("Received equals input user and friend ids: {}", userId);
            throw new ValidationException(Constants.IDS_SHOULD_BE_DIFFERENT_MESSAGE);
        }
    }
}
