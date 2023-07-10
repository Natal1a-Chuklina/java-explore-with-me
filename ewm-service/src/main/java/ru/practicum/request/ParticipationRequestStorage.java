package ru.practicum.request;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipationRequestStorage extends JpaRepository<ParticipationRequest, Long>, QuerydslPredicateExecutor<ParticipationRequest> {
    List<ParticipationRequest> findByEvent_IdAndStatus(Long id, RequestStatus status);

    int countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByRequester_Id(Long userId);

    Optional<ParticipationRequest> findByIdAndRequester_Id(Long requestId, Long userId);

    List<ParticipationRequest> findByIdInAndEvent_Id(Set<Long> requestIds, Long eventId);

    List<ParticipationRequest> findByEvent_Id(Long id);

    List<ParticipationRequest> findByEvent_IdInAndStatus(Collection<Long> ids, RequestStatus status);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"event", "requester", "event.category", "event.initiator"})
    Page<ParticipationRequest> findAll(Predicate predicate, Pageable pageable);
}
