package ru.practicum.explore.event.service;

import org.springframework.stereotype.Service;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.dto.*;
import ru.practicum.explore.event.search.AdminSearchCriteria;
import ru.practicum.explore.request.dto.RequestDto;
import ru.practicum.explore.request.dto.RequestUpdateDto;
import ru.practicum.explore.request.dto.RequestUpdateResultDto;
import ru.practicum.explore.event.search.PublicSearchCriteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public interface EventService {
    EventFullDto saveEvent(Long userId, EventDto eventDto);
    EventFullDto getEventByIdPrivate(Long userId, Long eventId);
    EventFullDto updateEventPrivate(Long userId, Long eventId, EventUpdateRequestUser eventUpdateRequestUser);
    EventFullDto updateEventByAdmin(Long eventId, EventUpdateRequestAdmin eventUpdateRequestAdmin);
    EventFullDto getEventByIdPublic(Long id, String uri, String ip);
    RequestUpdateResultDto updateStatusRequestsForEvent(Long userId, Long eventId, RequestUpdateDto requestUpdateDto);
    List<EventShortDto> getAllEventsByInitiatorPrivate(Long userId, int from, int size);
    List<RequestDto> getEventRequests(Long userId, Long eventId);
    List<EventFullDto> searchEventsAdmin(AdminSearchCriteria param);
    Set<EventShortDto> getEventsPublic(PublicSearchCriteria param);
   long getCountConfirmedRequestsByEvent(Event event);
//    Long getViews(Long eventId);
//    //void setViewsForListShortDto(List<? extends EventShortDto> events);
//    void setViewsForListShortDto(List<EventShortDto> events);


}
