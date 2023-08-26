package ru.practicum.explore.event.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.event.dto.EventFullDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.search.PublicSearchCriteria;
import ru.practicum.explore.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Set<EventShortDto>> getEventsPublic(@RequestParam(required = false) String text,
                                              @RequestParam(required = false) List<Long> categories,
                                              @RequestParam(required = false) Boolean paid,
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                              @RequestParam(required = false) LocalDateTime rangeStart,
                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                              @RequestParam(required = false) LocalDateTime rangeEnd,
                                              @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                              @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size,
                                              HttpServletRequest request) {
        log.info("Получен GET- запрос: /events,  text = {}, categories = {}, paid = {}, rangeStart = {}, " +
                        "rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        PublicSearchCriteria param = PublicSearchCriteria.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .request(request)
                .build();
        return new ResponseEntity<>(eventService.getEventsPublic(param), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получен GET- запрос (получить событие по идентификатору):  /events/{id} от пользователя. " +
                        "Просмотр события (id): {}, от client ip: {},  path: {}",
                id, request.getRemoteAddr(), request.getRequestURI());
        return eventService.getEventByIdPublic(id, request.getRequestURI(), request.getRemoteAddr());
    }
}
