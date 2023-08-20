package ru.practicum.explore.service;

import ru.practicum.explore.model.ModelHit;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.util.List;

public interface StatsService {
    ModelHit saveHit(HitDto hitDto);

    List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique);
}
