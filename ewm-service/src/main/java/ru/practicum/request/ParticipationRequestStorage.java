package ru.practicum.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ParticipationRequestStorage extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByEvent_IdAndStatus(Long id, RequestStatus status);

    int countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByRequester_Id(Long userId);

    Optional<ParticipationRequest> findByIdAndRequester_Id(Long requestId, Long userId);

    List<ParticipationRequest> findByIdInAndEvent_Id(Set<Long> requestIds, Long eventId);

    List<ParticipationRequest> findByEvent_Id(Long id);

    List<ParticipationRequest> findByEvent_IdInAndStatus(Collection<Long> ids, RequestStatus status);


}
