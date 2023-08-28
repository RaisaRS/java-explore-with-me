package ru.practicum.explore.event.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.event.dto.EventDto;
import ru.practicum.explore.event.dto.EventUpdateRequestAdmin;
import ru.practicum.explore.event.search.AdminSearchCriteria;
import ru.practicum.explore.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> searchEventsByAdmin(@RequestParam(name = "users", required = false) List<Long> users,
                                                              @RequestParam(name = "states", required = false) List<String> states,
                                                              @RequestParam(name = "categories", required = false) List<Long> categories,
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                              @RequestParam(name = "rangeStart", required = false) LocalDateTime rangeStart,
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                              @RequestParam(name = "rangeEnd", required = false) LocalDateTime rangeEnd,
                                                              @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                                              @RequestParam(name = "size", defaultValue = "10") @Positive int size) {


        List<EventState> statesEnum = null;
        if (states != null) {
            statesEnum = states.stream()
                    .map(EventState::from)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        AdminSearchCriteria param = AdminSearchCriteria.builder()
                .users(users)
                .states(statesEnum)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        log.info("Получен GET-запрос: /admin/events, параметры = {}", param);
        return new ResponseEntity<>(eventService.searchEventsAdmin(param), HttpStatus.OK);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventByAdmin(@PathVariable Long eventId,
                                       @Valid @RequestBody EventUpdateRequestAdmin eventUpdateRequestAdmin) {
        log.info("Получен PATCH- запрос: /admin/events/{eventId}] (Admin). Обновление события (id): {} обновлено (dto): {}",
                eventId, eventUpdateRequestAdmin);
        return eventService.updateEventByAdmin(eventId, eventUpdateRequestAdmin);
    }
}
