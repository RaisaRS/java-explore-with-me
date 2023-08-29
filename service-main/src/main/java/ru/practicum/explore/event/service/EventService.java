package ru.practicum.explore.event.service;

import org.springframework.stereotype.Service;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.search.AdminSearchCriteria;
import ru.practicum.explore.event.search.PublicSearchCriteria;
import ru.practicum.explore.request.dto.RequestDto;
import ru.practicum.explore.request.dto.RequestUpdateDto;
import ru.practicum.explore.request.dto.RequestUpdateResultDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Service
public interface EventService {
    EventFullDto saveEvent(Long userId, EventNewDto eventNewDto);

    EventFullDto getEventByIdPrivate(Long userId, Long eventId);

    EventFullDto updateEventPrivate(Long userId, Long eventId, EventUpdateRequestUser eventUpdateRequestUser);

    EventDto updateEventByAdmin(Long eventId, EventUpdateRequestAdmin eventUpdateRequestAdmin);

    EventDto getEventByIdPublic(Long eventId, HttpServletRequest request);

    RequestUpdateResultDto updateStatusRequestsForEvent(Long userId, Long eventId, RequestUpdateDto requestUpdateDto);

    List<EventShortDto> getAllEventsByInitiatorPrivate(Long userId, int from, int size);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    List<EventDto> searchEventsAdmin(AdminSearchCriteria param);

    Set<EventShortDto> getEventsPublic(PublicSearchCriteria param);

    long getCountConfirmedRequestsByEvent(Event event);
}
