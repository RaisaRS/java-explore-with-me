package ru.practicum.explore.event.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.EventShortDto;
import ru.practicum.explore.event.search.PublicSearchCriteria;
import ru.practicum.explore.event.service.EventService;
import ru.practicum.explore.exceptions.ParameterException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public ResponseEntity<Set<EventShortDto>> getEventsPublic(@RequestParam(name = "text", required = false) String text,
                                                              @RequestParam(name = "categories", required = false)
                                                              List<Long> categories,
                                                              @RequestParam(name = "paid", required = false)
                                                              Boolean paid,
                                                              @RequestParam(name = "rangeStart", required = false)
                                                              String rangeStart,
                                                              @RequestParam(name = "rangeEnd", required = false)
                                                              String rangeEnd,
                                                              @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                              @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                              @RequestParam(name = "from", defaultValue = "0")
                                                              @PositiveOrZero int from,
                                                              @RequestParam(name = "size", defaultValue = "10")
                                                              @Positive int size,
                                                              HttpServletRequest request) {
        log.info("Получен GET- запрос: /events,  text = {}, categories = {}, paid = {}, rangeStart = {}, " +
                        "rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, dateTimeFormatter);
        }
        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, dateTimeFormatter);
        }

        if (start != null && end != null) {
            if (start.isAfter(end)) {
                log.info("Start date {} is after end date {}.", start, end);
                throw new ParameterException(String.format("Start date %s is after end date %s.", start, end));
            }
        }

        PublicSearchCriteria param = PublicSearchCriteria.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(start)
                .rangeEnd(end)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .request(request)
                .build();
        return new ResponseEntity<>(eventService.getEventsPublic(param), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventByIdPublic(@PathVariable Long id, HttpServletRequest request) {
        log.info("Получен GET- запрос (получить событие по идентификатору):  /events/{id}  (id): {}, запрос: {}",
                id, request);
        return new ResponseEntity<>(eventService.getEventByIdPublic(id, request), HttpStatus.OK);
    }
}
