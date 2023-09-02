package ru.practicum.explore.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    @Size(max = 1000)
    @NotBlank
    private String text;
    private String authorName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    public LocalDateTime getCreated() {
        if (created == null) {
            created = LocalDateTime.now();
        }
        return created;
    }
}
