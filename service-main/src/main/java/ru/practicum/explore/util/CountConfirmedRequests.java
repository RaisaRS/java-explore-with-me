package ru.practicum.explore.util;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.request.Request;

import java.util.List;

@UtilityClass
public class CountConfirmedRequests {
    public long countConfirmedRequests(Event event) {
        List<Request> allRequests = event.getAllRequests();

        long result = 0L;
        if (allRequests != null) {
            for (Request request : allRequests) {
                if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                    result++;
                }
            }
        }
        return result;
    }
}
