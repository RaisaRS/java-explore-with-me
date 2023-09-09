package ru.practicum.explore.comments;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(Long eventId);

    @Query(value = "select c from  Comment c join fetch c.event e where e.state =?1 and e.id=?2 and c.id=?3")
    Optional<Comment> findByIdForEvent(String state, Long eventId, Long commentId);

    @Query(value = "select c from Comment c join fetch c.event e where e.state =?1 and e.id =?2")
    List<Comment> findAllByStateAndEventId(@Param(value = "state") String state, @Param(value = "eventId") Long eventId,
                                           PageRequest pageRequest);

    List<Comment> findTop10ByEventIdOrderByCreatedDesc(Long eventId);
}
