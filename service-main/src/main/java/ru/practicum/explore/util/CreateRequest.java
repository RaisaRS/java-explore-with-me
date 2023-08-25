package ru.practicum.explore.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import ru.practicum.explore.exceptions.ParameterException;

@UtilityClass
public class CreateRequest {
    public static PageRequest createRequest(int from, int size) {
        if (from < 0) {
            throw new ParameterException("Параметр должен быть больше нуля.");
        }
        if (size <= 0) {
            throw new ParameterException("Размер параметра должен быть больше или равен нулю.");
        }
        return PageRequest.of(from / size, size);
    }
}
