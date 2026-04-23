package com.projectbible.post.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class PostDtos {
    private PostDtos() {
    }

    public record AuthorDto(Long id, String nickname) {}

    public record PostListItemDto(
        Long id,
        Long boardId,
        String boardName,
        String title,
        int viewCount,
        int likeCount,
        int commentCount,
        String status,
        LocalDateTime createdAt,
        AuthorDto author
    ) {}

    public record PostDetailDto(
        Long id,
        Long boardId,
        String boardName,
        String title,
        String content,
        int viewCount,
        int likeCount,
        int commentCount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AuthorDto author
    ) {}

    public record CreatePostDto(@NotNull Long boardId, @NotBlank String title, @NotBlank String content) {}

    public record UpdatePostDto(String title, String content) {}
}
