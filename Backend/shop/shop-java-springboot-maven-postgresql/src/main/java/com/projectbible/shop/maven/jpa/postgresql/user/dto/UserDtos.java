package com.projectbible.shop.maven.jpa.postgresql.user.dto;

import java.time.LocalDateTime;

public final class UserDtos {
    private UserDtos() {}

    public record UserSummaryDto(Long id, String email, String name, String phone, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record UpdateUserDto(String name, String phone) {}
}
