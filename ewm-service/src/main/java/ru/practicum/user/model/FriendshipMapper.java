package ru.practicum.user.model;

public class FriendshipMapper {
    private FriendshipMapper() {
    }

    public static Friendship toFriendship(User user, User friend, FriendshipStatus status) {
        return new Friendship(
                user,
                friend,
                status
        );
    }
}
