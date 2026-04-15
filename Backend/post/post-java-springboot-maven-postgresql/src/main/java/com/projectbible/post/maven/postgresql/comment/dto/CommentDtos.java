package com.projectbible.post.maven.postgresql.comment.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import com.projectbible.post.maven.postgresql.post.dto.PostDtos.AuthorDto;

public final class CommentDtos {
    private CommentDtos() {
    }

    public record CommentResponseDto(Long id, Long postId, Long userId, String content, String status, LocalDateTime createdAt, LocalDateTime updatedAt, AuthorDto author) {}

    public record CreateCommentDto(@NotBlank String content) {}

    public record UpdateCommentDto(@NotBlank String content) {}
}
