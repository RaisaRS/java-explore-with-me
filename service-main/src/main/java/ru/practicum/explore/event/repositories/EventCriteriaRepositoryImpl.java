package ru.practicum.explore.event.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.search.CriteriaAdmin;
import ru.practicum.explore.event.search.CriteriaUser;
import ru.practicum.explore.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class EventCriteriaRepositoryImpl implements EventCriteriaRepository {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    public EventCriteriaRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    @Override
    public Page<Event> findByParamUser(Pageable pageable, CriteriaUser criteriaUser) {

        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = getUserPredicate(criteriaUser, eventRoot);
        criteriaQuery.where(predicate);

        if (pageable.getSort().isUnsorted()) {
            criteriaQuery.orderBy(criteriaBuilder.desc(eventRoot.get("createdOn")));
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> events = typedQuery.getResultList();

        return new PageImpl<>(events);
    }

    @Override
    public Page<Event> findByParamAdmin(Pageable pageable, CriteriaAdmin criteriaAdmin) {
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = getAdminPredicate(criteriaAdmin, eventRoot);
        criteriaQuery.where(predicate);

        if (pageable.getSort().isUnsorted()) {
            criteriaQuery.orderBy(criteriaBuilder.desc(eventRoot.get("createdOn")));
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> events = typedQuery.getResultList();

        return new PageImpl<>(events);
    }

    private Predicate getAdminPredicate(CriteriaAdmin criteriaAdmin, Root<Event> eventRoot) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteriaAdmin != null && criteriaAdmin.getCategories() != null && !criteriaAdmin.getCategories().isEmpty()) {
            Join<Event, Category> categoryJoin = eventRoot.join("category");
            predicates.add(categoryJoin.get("id").in(criteriaAdmin.getCategories()));
        }
        if (criteriaAdmin != null && criteriaAdmin.getUsers() != null && !criteriaAdmin.getUsers().isEmpty()) {
            Join<Event, User> userJoin = eventRoot.join("initiator");
            predicates.add(userJoin.get("id").in(criteriaAdmin.getUsers()));
        }
        if (criteriaAdmin != null && criteriaAdmin.getStates() != null && !criteriaAdmin.getStates().isEmpty()) {
            predicates.add(eventRoot.get("state").in(criteriaAdmin.getStates()));
        }


        if (criteriaAdmin != null && criteriaAdmin.getRangeStart() != null || criteriaAdmin.getRangeEnd() != null) {
            LocalDateTime rangeStart = criteriaAdmin.getRangeStart() != null
                    ? criteriaAdmin.getRangeStart()
                    : LocalDateTime.MIN;
            LocalDateTime rangeEnd = criteriaAdmin.getRangeEnd() != null
                    ? criteriaAdmin.getRangeEnd()
                    : LocalDateTime.MAX;
            predicates.add(criteriaBuilder.between(eventRoot.get("eventDate"), rangeStart, rangeEnd));
        } else {
            predicates.add(criteriaBuilder.between(eventRoot.get("eventDate"), LocalDateTime.now(),
                    LocalDateTime.now().plusYears(100)));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate getUserPredicate(CriteriaUser criteria, Root<Event> eventRoot) {
        List<Predicate> predicates = new ArrayList<>();
        Predicate annotationPredicate = null;
        if (Objects.nonNull(criteria.getText())) {
            annotationPredicate = criteriaBuilder.like(criteriaBuilder.lower(eventRoot.get("annotation")),
                    "%" + criteria.getText().toLowerCase() + "%");
        }
        if (Objects.nonNull(criteria.getText()) && annotationPredicate == null) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(eventRoot.get("description")),
                    "%" + criteria.getText().toLowerCase() + "%"));
        } else if (Objects.nonNull(criteria.getText())) {
            Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(eventRoot.get("description")),
                    "%" + criteria.getText().toLowerCase() + "%");
            predicates.add(criteriaBuilder.or(annotationPredicate, descriptionPredicate));
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            Join<Event, Category> categoryJoin = eventRoot.join("category");
            predicates.add(categoryJoin.get("id").in(criteria.getCategories()));
        }
        if (criteria.getPaid() != null && criteria.getPaid().equals(Boolean.TRUE)) {
            predicates.add(criteriaBuilder.equal(eventRoot.get("paid"), criteria.getPaid()));
        }
        if (criteria.getRangeStart() != null || criteria.getRangeEnd() != null) {
            LocalDateTime rangeStart = criteria.getRangeStart() != null
                    ? criteria.getRangeStart()
                    : LocalDateTime.MIN;
            LocalDateTime rangeEnd = criteria.getRangeEnd() != null
                    ? criteria.getRangeEnd()
                    : LocalDateTime.MAX;
            predicates.add(criteriaBuilder.between(eventRoot.get("eventDate"), rangeStart, rangeEnd));
        } else {
            predicates.add(criteriaBuilder.between(eventRoot.get("eventDate"), LocalDateTime.now(),
                    LocalDateTime.now().plusYears(100)));
        }

        if (criteria.getOnlyAvailable() != null && criteria.getOnlyAvailable()) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(eventRoot.get("participantLimit")),
                    criteriaBuilder.greaterThan(
                            criteriaBuilder.diff(eventRoot.get("participantLimit"), eventRoot.get("confirmedRequests")),
                            0L
                    )
            ));
        }
        return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }

}
