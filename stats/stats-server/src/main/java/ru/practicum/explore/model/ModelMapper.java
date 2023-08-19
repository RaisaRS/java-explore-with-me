package ru.practicum.explore.model;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ModelMapper {

    public static ModelHit toModelHit(HitDto hitDto) {
        return ModelHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }

    public static HitDto toHitDto(ModelHit modelHit) {
        return HitDto.builder()
                //.id(modelHit.getId())
                .app(modelHit.getApp())
                .uri(modelHit.getUri())
                .ip(modelHit.getIp())
                .timestamp(modelHit.getTimestamp())
                .build();
    }

    public static StatsDto toStatsDto(ModelStats modelStats) {
        return StatsDto.builder()
                .app(modelStats.getApp())
                .uri(modelStats.getUri())
                .hits(modelStats.getHits())
                .build();
    }

    public static List<StatsDto> statsDtos(List<ModelStats> modelStatsList) {
        return modelStatsList.stream()
                .map(ModelMapper::toStatsDto)
                .collect(Collectors.toList());
    }
}
