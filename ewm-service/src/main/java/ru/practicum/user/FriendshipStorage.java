package ru.practicum.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.Friendship;
import ru.practicum.user.model.FriendshipStatus;

import java.util.Collection;
import java.util.List;

public interface FriendshipStorage extends JpaRepository<Friendship, Long> {
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"friend"})
    List<Friendship> findByUser_IdAndStatusOrFriend_IdAndStatus(Long userId, FriendshipStatus status, Long friendId,
                                                                FriendshipStatus status1);

    List<Friendship> findByUser_IdInAndFriend_IdIn(Collection<Long> userIds, Collection<Long> friendIds);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"friend"})
    List<Friendship> findByUser_IdAndStatus(Long id, FriendshipStatus status);
}
