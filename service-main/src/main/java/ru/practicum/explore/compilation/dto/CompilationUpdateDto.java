package ru.practicum.explore.compilation.dto;

import lombok.*;

import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationUpdateDto {

    @Size(max = 50, min = 1)
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
