package ru.practicum.explore.request;

import lombok.*;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    @Column(name = "created")
    private LocalDateTime created;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

}
