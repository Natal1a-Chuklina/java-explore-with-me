package ru.practicum.event;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.List;
import java.util.Optional;

public interface EventStorage extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator", "location"})
    Optional<Event> findByIdAndInitiator_Id(Long eventId, Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator"})
    Page<Event> findByInitiator_Id(Long id, Pageable pageable);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator", "location"})
    Optional<Event> findByIdAndState(Long id, EventState state);

    boolean existsByIdAndInitiator_Id(Long eventId, Long userId);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"initiator", "location"})
    Optional<Event> findById(Long eventId);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator", "location"})
    Page<Event> findAll(Predicate predicate, Pageable pageable);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator", "location"})
    Page<Event> findAll(Pageable pageable);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"category", "initiator"})
    List<Event> findAllById(Iterable<Long> longs);
}
