package ru.practicum.explore.event;

import lombok.*;
import ru.practicum.explore.category.Category;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.location.Location;
import ru.practicum.explore.request.Request;
import ru.practicum.explore.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "events", schema = "public")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "annotation", nullable = false)
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @OneToMany
    @JoinColumn(name = "event_id")
    private List<Request> allRequests;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Column(name = "paid")
    private Boolean paid;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state;

}
