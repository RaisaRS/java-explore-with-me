package ru.practicum.explore.event.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.search.CriteriaAdmin;
import ru.practicum.explore.event.search.CriteriaUser;

@Repository
public interface EventCriteriaRepository {
    Page<Event> findByParamUser(Pageable pageable, CriteriaUser criteriaUser);

    Page<Event> findByParamAdmin(Pageable pageable, CriteriaAdmin criteria);
}
