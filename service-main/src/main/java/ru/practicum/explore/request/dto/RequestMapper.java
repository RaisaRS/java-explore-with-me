package ru.practicum.explore.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.request.Request;
import ru.practicum.explore.user.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class RequestMapper {
    public Request toRequestFromUserIdAndEventId(Long userId, Long eventId) {
        return Request.builder()
                .event(Event.builder()
                        .id(eventId)
                        .build())
                .requester(User.builder()
                        .id(userId)
                        .build())
                .build();
    }

    public Request toRequestFromUserAndEvent(User user, Event event) {
        Request request = new Request();
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(RequestStatus.PENDING);
        return request;
    }

    public RequestDto toRequestDto(Request request) {
        RequestDto requestDto = new RequestDto();
        requestDto.setId(request.getId());
        requestDto.setEvent(request.getEvent().getId());
        requestDto.setRequester(request.getRequester().getId());
        requestDto.setCreated(request.getCreated());
        requestDto.setStatus(request.getStatus());
        return requestDto;
    }

    public List<RequestDto> listRequestDtos(List<Request> requests) {
        return requests.stream()
                .map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());

    }
}
