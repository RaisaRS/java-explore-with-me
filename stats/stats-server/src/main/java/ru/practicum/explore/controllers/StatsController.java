package ru.practicum.explore.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.service.StatsService;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.util.List;

import static ru.practicum.explore.model.ModelMapper.toHitDto;

@RestController
@Slf4j
public class StatsController {

    private final StatsService statsService;


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

        log.info("[GET /stats?start={start}&end={end}&uris={uris}&unique={unique}]." +
                        " Запрошена статистика за период с даты: {} по дату: {} по uris: {} (unique: {})",
                start, end, uris, unique);

        return statsService.getStats(start, end, uris, unique);
    }
}
