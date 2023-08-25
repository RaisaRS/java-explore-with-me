package ru.practicum.explore.event.dto;

import lombok.*;
import ru.practicum.explore.enums.StateActionAdmin;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class EventUpdateRequestAdmin extends EventUpdateDto {
    private StateActionAdmin stateAction;
}
