package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.location.LocationDto;
import ru.practicum.explore.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {

    private Long id;
    private String title;
    private String description;
    private String annotation;
    private CategoryDto category;
    private UserShortDto initiator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Long confirmedRequests;
    private Boolean paid;
    private Long views;
    private LocationDto location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Long participantLimit;
    private Boolean requestModeration;
    private EventState state;
}
