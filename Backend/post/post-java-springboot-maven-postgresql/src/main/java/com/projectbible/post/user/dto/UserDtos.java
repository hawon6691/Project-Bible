package com.projectbible.post.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserSummaryDto(Long id, String email, String nickname, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record UpdateUserDto(@NotBlank String nickname, String password) {}
}
