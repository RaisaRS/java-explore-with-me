package ru.practicum.explore.event.dto;

import lombok.*;
import ru.practicum.explore.enums.StateActionUser;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventUpdateRequestUser extends EventUpdateDto {
    private StateActionUser stateAction;
}
