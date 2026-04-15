package com.projectbible.post.maven.jpa.postgresql.board.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class BoardDtos {
    private BoardDtos() {
    }

    public record BoardResponseDto(Long id, String name, String description, int displayOrder, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record UpsertBoardDto(@NotBlank String name, String description, Integer displayOrder, String status) {}
}
