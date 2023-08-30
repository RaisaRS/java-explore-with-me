package ru.practicum.explore.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.category.CategoryRepository;
import ru.practicum.explore.client.StatsClient;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.enums.StateActionAdmin;
import ru.practicum.explore.enums.StateActionUser;
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

import static ru.practicum.explore.enums.EventState.PUBLISHED;
import static ru.practicum.explore.enums.StateActionAdmin.PUBLISH_EVENT;


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

    @Value("${server.url}")
    private String serviceName;
    private final HashSet<String> requestHashSet = new HashSet<>();
    private final HashMap<Long, HashSet<String>> httpServletRequests = new HashMap<>();

    @Override
    @Transactional
    public EventFullDto saveEvent(Long userId, EventNewDto eventNewDto) {
        User user = getUser(userId);

        LocalDateTime minStartDate = LocalDateTime.now().plusHours(1);
        if (eventNewDto.getEventDate().isBefore(minStartDate)) {
            throw new ConflictException(String.format("Дата события не может быть ранее даты добавления на 1 час", 1));
        }

        Event createdEvent = EventMapper.toEvent(user, eventNewDto);
        Category category = getCategory(eventNewDto.getCategory());
        createdEvent.setCategory(category);
        createdEvent.setCreatedOn(LocalDateTime.now());
        createdEvent.setState(EventState.PENDING);

        List<Location> check = locationRepository.findByLatAndLon(eventNewDto.getLocation().getLat(),
                eventNewDto.getLocation().getLon());
        if (check.isEmpty()) {
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

    @Override
    @Transactional
    public EventFullDto getEventByIdPrivate(Long userId, Long eventId) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Событие с идентификатором id=%s and и пользователь с идентификатором id=%s не найдены",
                        eventId, userId)));

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(getViews(event));
        log.info("Просмотр события по идентификатору пользователем, который его добавил");
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventPrivate(Long userId, Long eventId, EventUpdateRequestUser eventDto) {

        Event eventToUpd = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Событие с идентификатором id=%s и пользователем его добавившим id=%s не найдено",
                        eventId, userId)));

        if (eventToUpd.getState().equals(PUBLISHED)) {
            throw new ConflictException("Нельзя обновить опубликованное событие");
        }

        if (eventDto.getEventDate() != null) {
            LocalDateTime minStartDate = LocalDateTime.now().plusHours(1);
            if (eventDto.getEventDate().isBefore(minStartDate)) {
                throw new ConflictException(String.format("Дата события не может быть ранее даты добавления на 1 час", 1));
            }
            eventToUpd.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getAnnotation() != null) {
            eventToUpd.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategory(eventDto.getCategory());
            eventToUpd.setCategory(category);
        }
        if (eventDto.getDescription() != null) {
            eventToUpd.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            List<Location> loc = locationRepository.findByLatAndLon(eventDto.getLocation().getLat(),
                    eventDto.getLocation().getLon());
            if (loc.size() == 0) {
                Location lc = new Location();
                lc.setLat(eventDto.getLocation().getLat());
                lc.setLon(eventDto.getLocation().getLon());
                var after = locationRepository.save(lc);
                eventToUpd.setLocation(after);
            } else {
                eventToUpd.setLocation(loc.get(0));
            }
        }
        if (eventDto.getPaid() != null) {
            eventToUpd.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            eventToUpd.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            eventToUpd.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            eventToUpd.setTitle(eventDto.getTitle());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(StateActionUser.SEND_TO_REVIEW)) {
                if (!eventToUpd.getState().equals(EventState.CANCELED)) {
                    throw new ConflictException("Невозможно отправить на проверку, если состояние не отменено");
                }
                eventToUpd.setState(EventState.PENDING);
            } else if (eventDto.getStateAction().equals(StateActionUser.CANCEL_REVIEW)) {
                if (!eventToUpd.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Невозможно отменить событие, если оно не находится в состоянии ожидания");
                }
                eventToUpd.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Некорректный статус");
            }
        }
        Event after = eventRepository.save(eventToUpd);
        return EventMapper.toEventFullDto(after);

    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, EventUpdateRequestAdmin eventDto) {
        Event eventToUpdAdmin = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие не найдено", eventId)));

        if (eventDto.getEventDate() != null) {
            var minStartDate = LocalDateTime.now().plusHours(1);
            if (eventDto.getEventDate().isBefore(minStartDate)) {
                throw new ConflictException(String.format("Событие не может быть ранее, " +
                        "чем за два часа до публикации", 1));
            }
            eventDto.setEventDate(eventDto.getEventDate());
        }
        if (eventDto.getAnnotation() != null) {
            eventToUpdAdmin.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategory(eventDto.getCategory());

            eventToUpdAdmin.setCategory(category);
        }
        if (eventDto.getDescription() != null) {
            eventToUpdAdmin.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            List<Location> loc = locationRepository.findByLatAndLon(eventDto.getLocation().getLat(),
                    eventDto.getLocation().getLon());
            if (loc.isEmpty()) {
                Location lc = new Location();
                lc.setLat(eventDto.getLocation().getLat());
                lc.setLon(eventDto.getLocation().getLon());
                var after = locationRepository.save(lc);
                eventToUpdAdmin.setLocation(after);
            } else {
                eventToUpdAdmin.setLocation(loc.get(0));
            }
        }
        if (eventDto.getPaid() != null) {
            eventToUpdAdmin.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            eventToUpdAdmin.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            eventToUpdAdmin.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            eventToUpdAdmin.setTitle(eventDto.getTitle());
        }
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(PUBLISH_EVENT)) {
                if (!eventToUpdAdmin.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Не удается опубликовать событие, поскольку оно не находится " +
                            "в состоянии ожидания");
                }
                LocalDateTime datePublish = LocalDateTime.now();
                LocalDateTime minStartDate = datePublish.plusHours(1);
                if (eventToUpdAdmin.getEventDate().isBefore(minStartDate)) {
                    throw new ConflictException(
                            String.format("Дата события должна быть не ранее, чем за час до публикации", 1));
                }
                eventToUpdAdmin.setState(PUBLISHED);
                eventToUpdAdmin.setPublishedOn(datePublish);
            } else if (eventDto.getStateAction().equals(StateActionAdmin.REJECT_EVENT)) {
                if (!eventToUpdAdmin.getState().equals(EventState.PENDING)) {
                    throw new ConflictException("Не удается отклонить событие, поскольку оно находится " +
                            "в опубликованном состоянии");
                }
                eventToUpdAdmin.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("Некорректный статус публикации");
            }
        }
        EventDto dto = EventMapper.toEventDto(eventRepository.save(eventToUpdAdmin));
        setConfirmedRequests(dto);
        log.info("Событие обновлено администратором");
        return dto;

    }

    @Override
    @Transactional
    public EventDto getEventByIdPublic(Long eventId, HttpServletRequest servletRequest) {

        requestHashSet.add(servletRequest.getRemoteAddr());
        httpServletRequests.put(eventId, requestHashSet);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = '" + eventId + " не найдено"));


        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Попытка получения информации о событии по публичному эндпоинту без публикации");
        }

        HitDto hitDto = HitDto.builder()
                .ip(servletRequest.getRemoteAddr())
                .uri(servletRequest.getRequestURI())
                .app(serviceName)
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveStats(hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());

        event.setViews((long) httpServletRequests.get(eventId).size());
        Event eventWithView = eventRepository.save(event);
        EventDto eventDto = EventMapper.toEventDto(eventWithView);

        log.info("Подробная информация о событи по id: {} ", eventId);

        return eventDto;
    }

    @Override
    @Transactional
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
    @Transactional
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        List<Request> listRequests = requestRepository.findByEventId(eventId);
        log.info("Список запросов на участие в событии");
        return RequestMapper.listRequestDtos(listRequests);
    }

    @Override
    @Transactional
    public List<EventDto> searchEventsAdmin(AdminSearchCriteria param) {
        LocalDateTime rangeEndTime = param.getRangeEnd();
        LocalDateTime rangeStartTime = param.getRangeStart();

        if (rangeStartTime != null && rangeEndTime != null) {
            if (rangeEndTime.isBefore(rangeStartTime)) {
                throw new ParameterException(
                        "Дата окончания мероприятия должна быть позже даты его начала.");
            }
        }

        Pageable pageable = PageRequest.of(param.getFrom() / param.getSize(), param.getSize(),
                org.springframework.data.domain
                        .Sort.by(org.springframework.data.domain
                                .Sort.Direction.ASC, "id"));

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
        List<EventDto> modifiedEventDtos = eventDtos.stream()
                .peek(eventDto -> eventDto.setViews(this.getViews(eventDto.getId())))
                .collect(Collectors.toList());
        List<EventDto> updatedEventDtos = modifiedEventDtos.stream()
                .map(eventDto -> {
                    Event event = events.stream()
                            .filter(e -> e.getId().equals(eventDto.getId()))
                            .findFirst()
                            .orElseThrow(NoSuchElementException::new);
                    Long confirmedRequests = this.getCountConfirmedRequestsByEvent(event);
                    eventDto.setConfirmedRequests(confirmedRequests);
                    return eventDto;
                })
                .collect(Collectors.toList());
        List<EventDto> resultEventDtos = new ArrayList<>(updatedEventDtos);

        return resultEventDtos;
    }


    @Override
    @Transactional
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

        CriteriaUser criteriaUser = CriteriaUser.builder()
                .text(param.getText())
                .categories(param.getCategories())
                .rangeEnd(rangeEndTime)
                .rangeStart(rangeStartTime)
                .paid(param.getPaid()).build();

        List<Event> events1 = eventRepository.findByParamUser(pageable, criteriaUser).toList();

        Set<EventShortDto> events = EventMapper.setEventShortDto(events1);
        log.info("Список событий: {}", events.size());

        HttpServletRequest request = param.getRequest();
        HitDto hitDto = HitDto.builder()
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .app("app.stats.url")
                .timestamp(LocalDateTime.now())
                .build();
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


    private EventDto setConfirmedRequests(EventDto eventDto) {
        eventDto.setConfirmedRequests(requestRepository.countByEventIdAndConfirmed(eventDto.getId()));
        return eventDto;
    }

    private void setConfirmedRequests(List<EventDto> events) {
        for (EventDto event : events) {
            event.setConfirmedRequests(requestRepository.countByEventIdAndConfirmed(event.getId()));
        }
    }

    private Category getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория события не найдена, id = " + categoryId));
        return category;

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
