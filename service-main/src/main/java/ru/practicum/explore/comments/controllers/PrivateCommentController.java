package ru.practicum.explore.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.comments.dto.CommentDto;
import ru.practicum.explore.comments.dto.CommentShortDto;
import ru.practicum.explore.comments.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/users/{userId}/events/{eventId}/comments")
public class PrivateCommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CommentShortDto saveComment(@PathVariable Long userId,
                                       @PathVariable Long eventId,
                                       @Valid @RequestBody CommentDto commentDto) {
        log.info("Получен POST-запрос users/{userId}/events/{eventId}/comments на добавление комментария {}" +
                " от пользователя (id): {} к событию (id): {}", commentDto, userId, eventId);
        return commentService.saveComment(userId, eventId, commentDto);
    }


    @PatchMapping("/{commentId}")
    public CommentShortDto updateComment(@PathVariable Long userId,
                                         @PathVariable Long eventId,
                                         @PathVariable Long commentId,
                                         @Valid @RequestBody CommentDto commentDto) {
        log.info("Получен PATCH- запрос users/{userId}/events/{eventId}/comments/{commentId} на обновление комментария " +
                        "(id): {} от пользователя (id): {} к событию (id): {}, комментарий: {}",
                commentId, userId, eventId, commentDto);
        return commentService.updateComment(userId, eventId, commentId, commentDto);
    }

    @GetMapping("/{commentId}")
    public CommentShortDto getCommentByIdForEvent(@PathVariable Long userId,
                                                  @PathVariable Long eventId,
                                                  @PathVariable Long commentId) {
        log.info("Получен GET- запрос /users/{userId}/events/{eventId}/comment/{commentId} (Private). " +
                "Просмотр комментария (id): {} fк событию (id): {}, от пользователя (id): {}", userId, eventId, commentId);
        return commentService.getCommentByIdForEvent(userId, eventId, commentId);
    }

    @GetMapping
    public List<CommentShortDto> getCommentsForEvent(@PathVariable Long userId,
                                                     @PathVariable Long eventId,
                                                     @RequestParam(required = false, defaultValue = "0") int from,
                                                     @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Получен GET- запрос /users/{userId}/events/{eventId}/comment/ (Private). " +
                        "Просмотр опубликованного комментариев (dto) к событию (id): {}, from: {} to: {}, от пользователя (id): {}",
                eventId, from, size, userId);
        return commentService.getCommentsForEvent(userId, eventId, size, from);
    }

    @DeleteMapping("/{commentId}")
    public void deletedComment(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @PathVariable Long commentId) {
        log.info("Получен DELETE-запрос /users/{userId}/events/{eventId}/comment/{commentId} (Private). " +
                "Удаление комментария(id): {} к событию (id): {}, от пользователя (id): {}", userId, eventId, commentId);
        commentService.deleteCommentByUser(userId, eventId, commentId);
    }
}
