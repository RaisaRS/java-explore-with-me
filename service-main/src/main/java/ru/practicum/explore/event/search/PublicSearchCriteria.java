package ru.practicum.explore.event.search;

import lombok.Builder;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PublicSearchCriteria {
    String text;
    List<Long> categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Boolean onlyAvailable;
    String sort;
    int from;
    int size;
    HttpServletRequest request;
}
