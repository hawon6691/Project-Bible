package com.projectbible.shop.auth.dto;

import com.projectbible.shop.user.dto.UserDtos.UserSummaryDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}

    public record SignupRequestDto(@Email @NotBlank String email, @NotBlank String password, @NotBlank String name, @NotBlank String phone) {}
    public record LoginRequestDto(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequestDto(@NotBlank String refreshToken) {}
    public record LoginResponseDto(String accessToken, String refreshToken, int expiresIn, UserSummaryDto user) {}
}
