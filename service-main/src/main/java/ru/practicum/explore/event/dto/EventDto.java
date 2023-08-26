package ru.practicum.explore.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explore.category.dto.CategoryDto;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.location.LocationDto;
import ru.practicum.explore.user.dto.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDto {
    Long id;
    String annotation;
    CategoryDto category;
    long confirmedRequests;
    LocalDateTime createdOn;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    UserDto initiator;
    LocationDto location;
    Boolean paid;
    Long participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;
    Boolean requestModeration;
    EventState state;
    String title;
    Long views;
//    private Long id;
//    @NotBlank
//    @Size(min = 3, max = 120)
//    private String title;
//    @NotBlank
//    @Size(min = 20, max = 7000)
//    private String description;
//    @NotBlank
//    @Size(min = 20, max = 2000)
//    private String annotation;
//    @NotNull
//    private Long category;
//    @NotNull
//    private LocationDto location;
//    @NotNull
//    @Future
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime eventDate;
//    private Integer participantLimit;
//    private Boolean requestModeration;
//    private Boolean paid;

//    public Integer getParticipantLimit() {
//        if (participantLimit == null) {
//            participantLimit = 0;
//        }
//        return participantLimit;
//    }
//
//    public Boolean getRequestModeration() {
//        if (requestModeration == null) {
//            requestModeration = true;
//        }
//        return requestModeration;
//    }
//
//    public Boolean getPaid() {
//        if (paid == null) {
//            paid = false;
//        }
//        return paid;
//    }
}
