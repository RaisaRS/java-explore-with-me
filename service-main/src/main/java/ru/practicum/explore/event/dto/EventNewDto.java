package ru.practicum.explore.event.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explore.event.search.Sort;
import ru.practicum.explore.location.LocationDto;
import ru.practicum.explore.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventNewDto {
    private String text;
    private Long[] categories;
    private Long category;
    private Boolean paid;
    private LocationDto location;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;

    @Builder.Default
    private Boolean onlyAvailable = false; //подумать как лучше

    @Builder.Default
    private Integer from = 0;

    @Builder.Default
    private Integer size = 10;

    private Sort sort;
    private Long views;

    private UserDto initiator;
    private Long requestId;

    private Long[] users;
}
