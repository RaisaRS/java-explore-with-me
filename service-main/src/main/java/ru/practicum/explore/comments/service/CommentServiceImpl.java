package ru.practicum.explore.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explore.comments.Comment;
import ru.practicum.explore.comments.CommentRepository;
import ru.practicum.explore.comments.dto.CommentDto;
import ru.practicum.explore.comments.dto.CommentMapper;
import ru.practicum.explore.comments.dto.CommentShortDto;
import ru.practicum.explore.enums.EventState;
import ru.practicum.explore.enums.RequestStatus;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.exceptions.NotFoundException;
import ru.practicum.explore.exceptions.ParameterException;
import ru.practicum.explore.request.RequestRepository;
import ru.practicum.explore.user.User;
import ru.practicum.explore.user.UserRepository;
import ru.practicum.explore.util.CreateRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    @Override
    public CommentShortDto saveComment(Long userId, Long eventId, CommentDto commentDto) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ParameterException("Комментарии доступны только к опубликованным событиям");
        }
        if (commentDto.getCreated().isAfter(event.getEventDate()) && (event.getRequestModeration() ||
                event.getParticipantLimit() != 0)) {
            boolean rq = requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId,
                    RequestStatus.CONFIRMED);
            if (!rq || !userId.equals(event.getInitiator().getId())) {
                throw new ParameterException("Только подтвержденный участник или инициатор события " +
                        "может оставлять комментарии");
            }
        }
        Comment after = commentRepository.save(CommentMapper.toComment(user, event, commentDto));
        return CommentMapper.toCommentShortDto(after);
    }

    @Override
    public CommentShortDto updateComment(Long userId, Long eventId, Long commentId, CommentDto commentDto) {
        getUser(userId);
        checkEvent(eventId);

        Comment comment = getComment(commentId);

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ParameterException("Только автор может изменить комментарий");
        }

        if (comment.getText().equals(commentDto.getText())) {
            throw new ParameterException("Комментарий не изменён");
        }

        comment.setText(commentDto.getText());
        Comment after = commentRepository.save(comment);

        return CommentMapper.toCommentShortDto(after);
    }

    @Override
    public void deleteCommentByUser(Long userId, Long eventId, Long commentId) {
        getUser(userId);
        Event event = getEvent(eventId);
        Comment comment = getComment(commentId);

        if (userId.equals(event.getInitiator().getId()) || userId.equals(comment.getAuthor().getId())) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ParameterException("Only author or event initiator can delete comment");
        }
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentShortDto getCommentByIdForEvent(Long userId, Long eventId, Long commentId) {
        getUser(userId);
        checkEvent(eventId);

        var commentById = commentRepository.findByIdForEvent(EventState.PUBLISHED.toString(),
                        eventId, commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%s was not found", commentId)));

        return CommentMapper.toCommentShortDto(commentById);
    }

    @Override
    public List<CommentShortDto> getCommentsForEvent(Long eventId, int size, int from) {
        checkEvent(eventId);

        PageRequest pageRequest = CreateRequest.createRequest(from, size);
        var pageAllComments = commentRepository.findAllByStateAndEventId(EventState.PUBLISHED.toString(),
                eventId, pageRequest);
        return CommentMapper.listToCommentShortDto(pageAllComments);
    }

    @Override
    public List<CommentShortDto> getCommentsForEvent(Long userId, Long eventId, int size, int from) {
        getUser(userId);
        checkEvent(eventId);

        PageRequest pageRequest = CreateRequest.createRequest(from, size);
        var pageAllCommentsForEvent = commentRepository.findAllByStateAndEventId(
                EventState.PUBLISHED.toString(), eventId, pageRequest);
        return CommentMapper.listToCommentShortDto(pageAllCommentsForEvent);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден id: " + userId));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден id:" + commentId));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено id:" + eventId));
    }

    private void checkEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие не найдено id" + eventId);
        }
    }

}
