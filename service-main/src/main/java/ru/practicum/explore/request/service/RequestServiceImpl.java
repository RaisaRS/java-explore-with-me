package ru.practicum.explore.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.exceptions.ConflictException;
import ru.practicum.explore.exceptions.NotFoundException;
import ru.practicum.explore.request.Request;
import ru.practicum.explore.request.RequestRepository;
import ru.practicum.explore.request.dto.RequestDto;
import ru.practicum.explore.request.dto.RequestMapper;
import ru.practicum.explore.user.User;
import ru.practicum.explore.user.UserRepository;
import ru.practicum.explore.util.CountConfirmedRequests;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;


    @Override
    @Transactional
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, id = " + userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено, id = " + eventId));

        Boolean isRequestExist = requestRepository.existsByEventIdAndRequesterId(eventId, userId);
        if (isRequestExist) {
            throw new ConflictException("Попытка добавления повторного запроса отклонена");
        }

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Ошибка запроса инициатора мероприятия");
        }
        if (!Objects.equals(event.getState(), EventState.PUBLISHED)) {
            throw new ConflictException("Невозможно принять участие в неопубликованном мероприятии");
        }
        if (event.getParticipantLimit() > 0) {
            Long countId = CountConfirmedRequests.countConfirmedRequests(event);
            if (event.getParticipantLimit() <= countId) {
                throw new ConflictException("Превышен лимит запросов на участие в мероприятии.");
            }
        }

        Request created = RequestMapper.toRequestFromUserAndEvent(user, event);
        created.setCreated(LocalDateTime.now());

        if (event.getRequestModeration().equals(false) || event.getParticipantLimit() == 0) {
            created.setStatus(RequestStatus.CONFIRMED);
        } else {
            created.setStatus(RequestStatus.PENDING);
        }

        Long confirmedRequests = eventService.getCountConfirmedRequestsByEvent(event);
        Long limit = event.getParticipantLimit();

        if (limit == 0) {
            created.setStatus(RequestStatus.CONFIRMED);
        } else if (confirmedRequests < limit) {
            if (!event.getRequestModeration()) {
                created.setStatus(RequestStatus.PENDING);
            }
        } else {
            throw new ConflictException("Свободные места на мероприятие закончились, id = " + eventId);
        }

        Request savedRequest = requestRepository.save(created);
        log.info("Создан запрос на участие в событии");
        return RequestMapper.toRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Запрос от пользователя не найден: requestId, userId" + requestId + userId));

        request.setStatus(RequestStatus.CANCELED);
        Request updatesRequest = requestRepository.save(request);
        log.info("Запрос на участие в событии отменён");
        return RequestMapper.toRequestDto(updatesRequest);
    }


    @Override
    public List<RequestDto> getAllRequestsParticipationInOtherPeoplesEvents(Long userId) {
        List<Request> requests = requestRepository.findByRequesterId(userId);
        log.info("Список запросов на участие в чужих событиях");
        return requests.isEmpty() ? Collections.emptyList() : RequestMapper.listRequestDtos(requests);
    }
}
