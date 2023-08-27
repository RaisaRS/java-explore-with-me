package ru.practicum.explore.event.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.Event;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventCriteriaRepository {
    Page<Event> findAllByInitiatorId(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndState(Long eventId, EventState eventState);

    @Query(value = "SELECT * FROM EVENTS WHERE CATEGORY_ID = ?1", nativeQuery = true)
    List<Event> findByCategoryId(Long catId);
}
