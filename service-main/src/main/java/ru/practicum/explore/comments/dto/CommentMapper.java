package ru.practicum.explore.comments.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.comments.Comment;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.user.User;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CommentMapper {

    public Comment toComment(User user, Event event, CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setId(commentDto.getId());
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setText(commentDto.getText());
        comment.setCreated(commentDto.getCreated());
        return comment;
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor().getName())
                .build();
    }

    public List<CommentDto> listToCommentDto(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        List<CommentDto> listCommentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            listCommentDtos.add(toCommentDto(comment));
        }
        return listCommentDtos;
    }

    public CommentShortDto toCommentShortDto(Comment comment) {
        CommentShortDto dto = new CommentShortDto();
        dto.setId(comment.getId());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public List<CommentShortDto> listToCommentShortDto(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        List<CommentShortDto> listDtos = new ArrayList<>();
        for (Comment comment : comments) {
            listDtos.add(toCommentShortDto(comment));
        }
        return listDtos;
    }
}
