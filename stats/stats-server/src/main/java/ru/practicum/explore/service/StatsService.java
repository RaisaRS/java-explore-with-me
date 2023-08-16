package ru.practicum.explore.service;

import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;
import ru.practicum.explore.model.ModelHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    ModelHit saveHit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
