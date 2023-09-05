package ru.practicum.explore.comments.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.comments.dto.CommentShortDto;
import ru.practicum.explore.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentShortDto> getCommentsForEvent(@PathVariable Long eventId,
                                                     @RequestParam(required = false, defaultValue = "0")
                                                     int from,
                                                     @RequestParam(required = false, defaultValue = "10")
                                                     int size) {
        log.info("Получен GET-запрос /events/{eventId}/comments (Public). " +
                "Просмотр опубликованных комментариев (dto) к событию (id): {}, from: {} to: {}", eventId, from, size);
        return commentService.getCommentsForEvent(eventId, size, from);
    }
}
