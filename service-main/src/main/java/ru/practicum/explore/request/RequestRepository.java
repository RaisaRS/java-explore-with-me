package ru.practicum.explore.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explore.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findByEventId(Long eventId);

    @Query(value = "select  count(r.id) from Request r where r.event.id =:eventId " +
            "and (r.status = 'CONFIRMED' or r.status = 'PENDING')")
    Integer countByEventId(@Param(value = "eventId") Long eventId);

    @Query(value = "select  COALESCE(count(r.id), 0) from Request r where r.event.id =:eventId " +
            "and r.status = 'CONFIRMED'")
    Long countByEventIdAndConfirmed(@Param(value = "eventId") Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long userId, Long eventId, RequestStatus status);
}
