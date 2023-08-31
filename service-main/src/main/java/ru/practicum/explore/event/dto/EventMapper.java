package ru.practicum.explore.event.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.category.dto.CategoryMapper;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.location.Location;
import ru.practicum.explore.location.LocationDto;
import ru.practicum.explore.user.User;
import ru.practicum.explore.user.dto.UserMapper;
import ru.practicum.explore.util.CountConfirmedRequests;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class EventMapper {
    public Event toEvent(EventDto dto) {

        return Event.builder()
                .annotation(dto.getAnnotation())
                .createdOn(LocalDateTime.now())
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .location(new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(EventState.PENDING)
                .title(dto.getTitle())
                .build();
    }

    public Event toEvent(User user, EventNewDto dto) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setAnnotation(dto.getAnnotation());
        event.setInitiator(user);
        event.setEventDate(dto.getEventDate());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setPaid(dto.getPaid());
        return event;
    }

    public EventShortDto toEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setDescription(event.getDescription());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.toUserDto(event.getInitiator()));
        eventShortDto.setConfirmedRequests(CountConfirmedRequests.countConfirmedRequests(event));
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setPaid(event.getPaid());
        return eventShortDto;
    }

    public Set<EventShortDto> setEventShortDto(List<Event> events) {
        Set<EventShortDto> evShortDtos = new HashSet<>();
        for (Event e : events) {
            evShortDtos.add(toEventShortDto(e));
        }
        return evShortDtos;
    }

    public List<EventShortDto> listEventShortDto(List<Event> events) {
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Event e : events) {
            eventShortDtos.add(toEventShortDto(e));
        }
        return eventShortDtos;
    }


    public EventFullDto toEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setLocation(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()));
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        eventFullDto.setConfirmedRequests(CountConfirmedRequests.countConfirmedRequests(event));
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setInitiator(UserMapper.toUserDtoShort(event.getInitiator()));
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setState(event.getState());
        return eventFullDto;
    }

    public EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.toUserDto(event.getInitiator()))
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public List<EventFullDto> listEventFullDto(List<Event> events) {
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

}
