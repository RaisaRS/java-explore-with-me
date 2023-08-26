package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explore.location.LocationDto;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventNewDto {
    private Long id;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    private Long category;

    @NotNull
    private LocationDto location;

    @NotNull
    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Long participantLimit;

    private Boolean requestModeration;

    private Boolean paid;

    public Long getParticipantLimit() {
        if (participantLimit == null) {
            participantLimit = 0L;
        }
        return participantLimit;
    }

    public Boolean getRequestModeration() {
        if (requestModeration == null) {
            requestModeration = true;
        }
        return requestModeration;
    }

    public Boolean getPaid() {
        if (paid == null) {
            paid = false;
        }
        return paid;
    }
}
