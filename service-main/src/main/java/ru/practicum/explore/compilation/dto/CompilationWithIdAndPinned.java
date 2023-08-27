package ru.practicum.explore.compilation.dto;

import lombok.*;
import ru.practicum.explore.event.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationWithIdAndPinned {
    private Long id;
    private String title;
    private Boolean pinned;
    private List<EventShortDto> events;
}
