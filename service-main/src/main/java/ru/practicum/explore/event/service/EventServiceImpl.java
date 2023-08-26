package ru.practicum.explore.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.category.CategoryRepository;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.enums.StateActionAdmin;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.event.search.AdminSearchCriteria;
import ru.practicum.explore.event.search.CriteriaAdmin;
import ru.practicum.explore.event.search.CriteriaUser;
import ru.practicum.explore.event.search.PublicSearchCriteria;
import ru.practicum.explore.exceptions.ConflictException;
import ru.practicum.explore.exceptions.NotFoundException;
import ru.practicum.explore.exceptions.ParameterException;
import ru.practicum.explore.location.Location;
import ru.practicum.explore.location.LocationRepository;
import ru.practicum.explore.request.Request;
import ru.practicum.explore.request.RequestRepository;
import ru.practicum.explore.request.dto.RequestDto;
import ru.practicum.explore.request.dto.RequestMapper;
import ru.practicum.explore.request.dto.RequestUpdateDto;
import ru.practicum.explore.request.dto.RequestUpdateResultDto;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;
import ru.practicum.explore.user.User;
import ru.practicum.explore.user.UserRepository;
import ru.practicum.explore.util.CountConfirmedRequests;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    private final StatsClient statsClient;
    private final CategoryRepository categoryRepository;

    @Override
    public EventFullDto saveEvent(Long userId, EventNewDto eventNewDto) {
        User user = getUser(userId);

        LocalDateTime minStartDate = LocalDateTime.now().plusHours(1);
        if (eventNewDto.getEventDate().isBefore(minStartDate)) {
            throw new ConflictException(String.format("Event date must be not earlier than %s hours later", 1));
        }

        Event createdEvent = EventMapper.toEvent(user, eventNewDto);
        Category category = getCategory(eventNewDto.getCategory());
        createdEvent.setCategory(category);
        createdEvent.setCreatedOn(LocalDateTime.now());
        createdEvent.setState(EventState.PENDING);

        List<Location> check = locationRepository.findByLatAndLon(eventNewDto.getLocation().getLat(),
                eventNewDto.getLocation().getLon());
        if (check.size() == 0) {
            Location lc = new Location();
            lc.setLat(eventNewDto.getLocation().getLat());
            lc.setLon(eventNewDto.getLocation().getLon());
            Location after = locationRepository.save(lc);
            createdEvent.setLocation(after);
        } else {
            createdEvent.setLocation(check.get(0));
        }

        Event afterCreate = eventRepository.save(createdEvent);
        return EventMapper.toEventFullDto(afterCreate);
    }

    private EventDto setConfirmedRequests(EventDto eventDto) {
        eventDto.setConfirmedRequests(requestRepository.countByEventIdAndConfirmed(eventDto.getId()));
        return eventDto;
    }

    private EventFullDto setConfirmedRequests(EventFullDto eventDto) {
        eventDto.setConfirmedRequests(requestRepository.countByEventIdAndConfirmed(eventDto.getId()));
        return eventDto;
    }

    private void setConfirmedRequests(List<EventDto> events) {
        for (EventDto event : events) {
            event.setConfirmedRequests(requestRepository.countByEventIdAndConfirmed(event.getId()));
        }
    }

    @Override
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {
        Event event = getEvent(eventId, userId);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(getViews(event));
        //setConfirmedRequests(eventFullDto);
        log.info("Cобытие {} запрошено по id пользователя {} , администратором", eventFullDto, userId);
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, EventUpdateRequestUser eventUpdateRequestUser) {
        Event eventForUpdate = getEvent(eventId);

        if (eventForUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Попытка обновления уже опубликованного события");
        }

        updateEventFields(eventForUpdate, eventUpdateRequestUser);

        if (eventForUpdate.getState() != null) {
            switch (eventUpdateRequestUser.getStateAction()) {
                case CANCEL_REVIEW:
                    eventForUpdate.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    eventForUpdate.setState(EventState.PENDING);
            }
        }

        EventFullDto updated = EventMapper.toEventFullDto(eventRepository.save(eventForUpdate));
        setConfirmedRequests(updated);
        log.info("Обновлён статус события пользователем");
        return updated;
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, EventUpdateRequestAdmin eventUpdateRequestAdmin) {
        Event eventToUpdateAdmin = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Попытка обновления уже опубликованного события " +
                        "от имени администратора, id события: " + eventId));

        switch (eventToUpdateAdmin.getState()) {
            case PUBLISHED:
                if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                    log.error("Событие уже опубликовано");
                    throw new ConflictException("Событие уже опубликовано");
                } else if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                    log.error("Невозможно отменить опубликованное событие");
                    throw new ConflictException("Невозможно отменить опубликованное событие");
                }
                break;
            case CANCELED:
                if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                    log.error("Не удается опубликовать отклоненное событие");
                    throw new ConflictException("Не удается опубликовать отклоненное событие");
                } else if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                    log.error("Событие, уже отклонено");
                    throw new ConflictException("Событие, уже отклонено");
                }
                break;
            case PENDING:
                if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                    eventToUpdateAdmin.setState(EventState.PUBLISHED);
                } else if (eventUpdateRequestAdmin.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                    eventToUpdateAdmin.setState(EventState.CANCELED);
                }
                eventToUpdateAdmin.setPublishedOn(LocalDateTime.now());
                break;
        }

        updateEventFields(eventToUpdateAdmin, eventUpdateRequestAdmin);

        EventFullDto eventUpdated = EventMapper.toEventFullDto(eventRepository.save(eventToUpdateAdmin));
        setConfirmedRequests(eventUpdated);
        log.info("Событие, обновленное администратором: {}.", eventUpdated);


        return eventUpdated;
    }

    @Override
    public EventFullDto getEventByIdPublic(Long id, String uri, String ip) {
        Event foundEvent = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено, id: " + id));

        LocalDateTime dateTimeNow = LocalDateTime.now();
        //statsClient.saveStats("service-main", uri, ip, dateTimeNow);
        Long viewsFromStats = getViews(foundEvent);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(foundEvent);
        eventFullDto.setViews(viewsFromStats);
        log.info("Событие {} запрошено пользователем", eventFullDto);
        return eventFullDto;

    }

    @Override
    public RequestUpdateResultDto updateStatusRequestsForEvent(Long userId, Long eventId,
                                                               RequestUpdateDto requestUpdateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие, добавленное пользователем id: , не найдено" + eventId + userId));

        long confirmedRequests = getCountConfirmedRequestsByEvent(event);
        if (confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участия в мероприятии был достигнут");
        }

        RequestUpdateResultDto afterUpdateStatus = new RequestUpdateResultDto();

        Map<Long, Request> allRequests = requestRepository.findAllById(requestUpdateDto.getRequestIds()).stream()
                .collect(Collectors.toMap(Request::getId, i -> i));

        List<Request> selectedRequests = requestUpdateDto.getRequestIds()
                .stream()
                .map(allRequests::get)
                .collect(Collectors.toList());

        if (selectedRequests.stream().anyMatch(Objects::isNull)) {
            throw new ParameterException("Запрос не найден для этого события");
        }

        boolean check = selectedRequests.stream()
                .anyMatch(r -> !Objects.equals(r.getStatus(), RequestStatus.PENDING));
        if (check) {
            throw new ConflictException("Запрос должен иметь статус PENDING");
        }

        if (event.getRequestModeration().equals(true) || event.getParticipantLimit() != 0) {
            long confReq = CountConfirmedRequests.countConfirmedRequests(event);

            for (Request request : selectedRequests) {
                if (event.getParticipantLimit() > confReq) {
                    if (requestUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confReq++;
                        if (event.getParticipantLimit() == confReq) {
                            for (Request rm : event.getAllRequests()) {
                                if (rm.getStatus().equals(RequestStatus.PENDING)) {
                                    rm.setStatus(RequestStatus.REJECTED);
                                }
                            }
                        }
                        afterUpdateStatus.getConfirmedRequests().add(RequestMapper.toRequestDto(request));
                    }
                    if (requestUpdateDto.getStatus().equals(RequestStatus.REJECTED)) {
                        request.setStatus(RequestStatus.REJECTED);
                        afterUpdateStatus.getRejectedRequests().add(RequestMapper.toRequestDto(request));
                    }
                } else {
                    if (requestUpdateDto.getStatus().equals(RequestStatus.CONFIRMED)) {
                        throw new ConflictException("Количество участников ограничено");
                    }
                }
            }
        }
        requestRepository.saveAll(selectedRequests);
        eventRepository.save(event);
        log.info("Обновлён статус запроса на участие в событии");
        return afterUpdateStatus;
    }

    @Override
    public List<EventShortDto> getAllEventsByInitiatorPrivate(Long userId, int from, int size) {
        PageRequest pageRequest = createRequest(from, size);
        List<Event> result = eventRepository.findAllByInitiatorId(userId, pageRequest).getContent();
        log.info("Все события, созданные инициатором");
        return result.isEmpty() ? Collections.emptyList() : EventMapper.listEventShortDto(result);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        List<Request> listRequests = requestRepository.findByEventId(eventId);
        log.info("Список запросов на участие в событии");
        return RequestMapper.listRequestDtos(listRequests);
    }

    @Override
    public List<EventDto> searchEventsAdmin(AdminSearchCriteria param) {
        LocalDateTime rangeEndTime = param.getRangeEnd();
        LocalDateTime rangeStartTime = param.getRangeStart();

        if (rangeStartTime != null && rangeEndTime != null) {
            if (rangeEndTime.isBefore(rangeStartTime)) {
                throw new ParameterException("Дата окончания мероприятия должна быть позже даты его начала.");
            }
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(),
                org.springframework.data.domain
                        .Sort.by(org.springframework.data.domain
                                .Sort.Direction.ASC, "id"));
// тут тест
        CriteriaAdmin criteria = CriteriaAdmin.builder()
                .users(param.getUsers())
                .states(param.getStates())
                .categories(param.getCategories())
                .rangeEnd(rangeEndTime)
                .rangeStart(rangeStartTime)
                .build();

        List<Event> events = eventRepository.findByParamAdmin(pageable, criteria).toList();
        events.stream()
                .map(EventMapper::toEventDto)
                .collect(Collectors.toList());

        List<EventDto> eventDtos = events.stream()
                .map(EventMapper::toEventDto)
                .collect(Collectors.toList());


        log.info("events.size() {}", events.size());
        return eventDtos.stream()
                .peek(eventDto -> eventDto.setViews(this.getViews(eventDto.getId())))
                .collect(Collectors.toList());

    }

    @Override
    public Set<EventShortDto> getEventsPublic(PublicSearchCriteria param) {

        LocalDateTime rangeEndTime = param.getRangeEnd();
        LocalDateTime rangeStartTime = param.getRangeStart();

        if (rangeStartTime != null && rangeEndTime != null) {
            if (rangeEndTime.isBefore(rangeStartTime)) {
                throw new ParameterException("Конечная дата должна быть ранее начальной");
            }
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
                        "id"));

        CriteriaUser criteriaUser = CriteriaUser.builder().text(param.getText()).categories(param.getCategories())
                .rangeEnd(rangeEndTime).rangeStart(rangeStartTime).paid(param.getPaid()).build();

        List<Event> events1 = eventRepository.findByParamUser(pageable, criteriaUser).toList();

        Set<EventShortDto> events = EventMapper.setEventShortDto(events1);
        log.info("  {}", events.size());

        HttpServletRequest request = param.getRequest();
        HitDto hitDto = HitDto.builder().ip(request.getRemoteAddr()).uri(request
                .getRequestURI()).app("app.stats.url").timestamp(LocalDateTime.now()).build();
        statsClient.saveStats(hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());
        log.info(" events.size()= {}  ", events.size());
        return events.stream()
                .peek(eventDtos -> eventDtos
                        .setViews(this.getViews(eventDtos.getId())))
                .collect(Collectors.toSet());
    }

    @Override
    public long getCountConfirmedRequestsByEvent(Event event) {
        List<Request> requests = requestRepository.findByEventId(event.getId());
        return !event.getRequestModeration()
                ? requests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED) ||
                        request.getStatus().equals(RequestStatus.PENDING)).count()
                : requests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED)).count();
    }


    private Long getViews(Long eventId) {
        Event event = eventRepository.getReferenceById(eventId);
        String[] uris = {"/events/" + eventId};
        List<StatsDto> stats = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(),
                List.of(uris), true);

        return stats
                .stream()
                .map(StatsDto::getHits)
                .findFirst()
                .orElse(0L);
    }

    private Long getViews(Event event) {
        long id = event.getId();
        String[] uris = {"/events/" + id};
        List<StatsDto> stats = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), List.of(uris), true);

        return stats
                .stream()
                .map(StatsDto::getHits)
                .findFirst()
                .orElse(0L);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User with id=%s was not found", userId)));
    }

    private Event getEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Event with id=%s and added by user id=%s was not found", eventId, userId)));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> {
            log.error("Event id {} not found.", eventId);
            return new NotFoundException(String.format("Event id %s not found.", eventId));
        });
    }

//    private Location getLocation(LocationDto locationDto) {
//        log.info("Проверка местоположения: {}.", locationDto);
//
//        Float latitude = locationDto.getLat();
//        Float longitude = locationDto.getLon();
//
//        Location savedLocation = findOrCreateLocation(latitude, longitude);
//        log.info("Сохраненное местоположение: {}.", savedLocation);
//        return savedLocation;
//    }
//
//    private Location findOrCreateLocation(Float latitude, Float longitude) {
//        List<Location> existingLocation = locationRepository.findByLatAndLon(latitude, longitude);
//        return existingLocation.orElseGet(() -> createLocation(latitude, longitude));
//    }
//
//    private Location createLocation(Float latitude, Float longitude) {
//        Location newLocation = new Location(latitude, longitude);
//        return locationRepository.save(newLocation);
//    }

    private Category getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория события не найдена, id = " + categoryId));
        return category;

    }

    private Request getRequest(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> {
            log.info("Запрос на участие, с id {} не найден.", requestId);
            return new NotFoundException("Запрос на участие не найден, id: " + requestId);
        });
    }

    private void updateEventFields(Event eventForUpdate, EventUpdateDto eventUpdateDto) {
        if (eventUpdateDto.getAnnotation() != null) {
            eventForUpdate.setAnnotation(eventUpdateDto.getAnnotation());
            log.debug("Аннотация обновлена до: {}", eventUpdateDto.getAnnotation());
        }

        if (eventUpdateDto.getCategory() != null
                && !eventUpdateDto.getCategory().equals(eventForUpdate.getCategory().getId())) {
            Category category = getCategory(eventUpdateDto.getCategory());
            eventForUpdate.setCategory(category);
            log.debug("Категория обновлена до: {}", category);
        }

        if (eventUpdateDto.getDescription() != null) {
            eventForUpdate.setDescription(eventUpdateDto.getDescription());
            log.debug("Описание обновлено до: {}", eventUpdateDto.getDescription());
        }

        if (eventUpdateDto.getEventDate() != null) {
            eventForUpdate.setEventDate(eventUpdateDto.getEventDate());
            log.debug("Дата события обновлена до: {}", eventUpdateDto.getEventDate());
        }

        if (eventUpdateDto.getLocation() != null) {
            var loc = locationRepository.findByLatAndLon(eventUpdateDto.getLocation().getLat(),
                    eventUpdateDto.getLocation().getLon());
            if (loc.size() == 0) {
                Location lc = new Location();
                lc.setLat(eventUpdateDto.getLocation().getLat());
                lc.setLon(eventUpdateDto.getLocation().getLon());
                var after = locationRepository.save(lc);
                eventForUpdate.setLocation(after);
            } else {
                eventForUpdate.setLocation(loc.get(0));
            }
        }

        if (eventUpdateDto.getPaid() != null) {
            eventForUpdate.setPaid(eventUpdateDto.getPaid());
            log.debug("Оплата участия в событии обновлена: {}", eventUpdateDto.getPaid());
        }

        if (eventUpdateDto.getParticipantLimit() != null) {
            eventForUpdate.setParticipantLimit(eventUpdateDto.getParticipantLimit());
            log.debug("Лимит участников обновлен до: {}", eventUpdateDto.getParticipantLimit());
        }

        if (eventUpdateDto.getRequestModeration() != null) {
            eventForUpdate.setRequestModeration(eventUpdateDto.getRequestModeration());
            log.debug("Модерация запроса обновлена до: {}", eventUpdateDto.getRequestModeration());
        }

        if (eventUpdateDto.getTitle() != null) {
            eventForUpdate.setTitle(eventUpdateDto.getTitle());
            log.debug("Название обновлено до: {}", eventUpdateDto.getTitle());
        }
    }

    private static PageRequest createRequest(int from, int size) {
        if (from < 0) {
            throw new ParameterException("Параметр from должен быть положительным или нулевым.");
        }
        if (size <= 0) {
            throw new ParameterException("Размер параметра должен быть положительным.");
        }
        return PageRequest.of(from / size, size);
    }
}
