package ru.practicum.explore.comments;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Length(min = 2, max = 1000)
    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
