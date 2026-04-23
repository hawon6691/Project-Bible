package com.projectbible.shop.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public final class AdminDtos {
    private AdminDtos() {}

    public record AdminSummaryDto(Long id, String email, String name, String status, LocalDateTime createdAt) {}
    public record AdminLoginRequestDto(@Email @NotBlank String email, @NotBlank String password) {}
    public record AdminRefreshRequestDto(@NotBlank String refreshToken) {}
    public record AdminLoginResponseDto(String accessToken, String refreshToken, int expiresIn, AdminSummaryDto admin) {}
    public record AdminDashboardResponseDto(int categoryCount, int productCount, int userCount, int reviewCount, int pendingOrderCount) {}
    public record StatusUpdateDto(@NotBlank String status) {}
    public record StatusResponseDto(Long id, String status, LocalDateTime updatedAt) {}
}
