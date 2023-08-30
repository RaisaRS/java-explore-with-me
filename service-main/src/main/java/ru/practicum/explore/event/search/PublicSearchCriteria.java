package ru.practicum.explore.event.search;

import lombok.Builder;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PublicSearchCriteria {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private String sort;
    private int from;
    private int size;
    private HttpServletRequest request;
}
