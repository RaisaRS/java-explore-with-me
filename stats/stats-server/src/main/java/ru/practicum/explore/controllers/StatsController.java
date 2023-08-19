package ru.practicum.explore.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.service.StatsService;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.explore.model.ModelMapper.toHitDto;

@RestController
@Slf4j
public class StatsController {

    private final StatsService statsService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public HitDto addHit(@RequestBody HitDto hitDto) {
        log.info("[POST /hit]. Создан запрос по (app: {}, client ip: {}, endpoint path: {}, time: {})",
                hitDto.getApp(), hitDto.getIp(), hitDto.getUri(), hitDto.getTimestamp());
        return toHitDto(statsService.saveHit(hitDto));

    }

    @GetMapping("/stats")
    public List<StatsDto> getStats(@RequestParam String start,
                                   @RequestParam String end,
                                   @RequestParam(required = false) List<String> uris,
                                   @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(start, dateTimeFormatter);
        LocalDateTime endDate = LocalDateTime.parse(end, dateTimeFormatter);
        if (startDate.isAfter(endDate)) {
            log.warn("Дата начала {} должна быть ранее даты окончания {}.", start, end);
            throw new IllegalArgumentException("Дата начала {} должна быть ранее даты окончания {}." + start + end);
        }

        var result = statsService.getStats(startDate, endDate, uris, unique);

        log.info("[GET /stats?start={start}&end={end}&uris={uris}&unique={unique}]." +
                        " Запрошена статистика за период с даты: {} по дату: {} по uris: {} (unique: {})",
                start, end, uris, unique);
        return result;
    }
}
