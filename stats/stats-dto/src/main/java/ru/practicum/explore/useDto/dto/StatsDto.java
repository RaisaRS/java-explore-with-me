package ru.practicum.explore.useDto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class StatsDto {
    private String app;
    private String uri;
    private Long hits;
}
