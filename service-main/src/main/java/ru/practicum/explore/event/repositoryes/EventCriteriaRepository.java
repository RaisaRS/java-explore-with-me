package ru.practicum.explore.event.repositoryes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.search.AdminSearchCriteria;
import ru.practicum.explore.event.search.CriteriaUser;
import ru.practicum.explore.event.search.PublicSearchCriteria;

@Repository
public interface EventCriteriaRepository {
    Page<Event> findByParamUser(Pageable pageable, CriteriaUser criteriaUser);

    Page<Event> findByParamAdmin(Pageable pageable, AdminSearchCriteria criteria);
}
