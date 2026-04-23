package com.projectbible.post.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.projectbible.post.auth.dto.AuthDtos.LoginRequestDto;
import com.projectbible.post.auth.dto.AuthDtos.LoginResponseDto;
import com.projectbible.post.auth.dto.AuthDtos.RefreshRequestDto;
import com.projectbible.post.auth.dto.AuthDtos.SignupRequestDto;
import com.projectbible.post.auth.service.AuthService;
import com.projectbible.post.common.api.ApiResponse;
import com.projectbible.post.common.api.MessageResponse;
import com.projectbible.post.common.security.AuthContext;
import com.projectbible.post.user.dto.UserDtos.UserSummaryDto;

@Tag(name = "auth")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User signup")
    @PostMapping("/signup")
    public ApiResponse<UserSummaryDto> signup(@Valid @RequestBody SignupRequestDto body) {
        return ApiResponse.success(authService.signup(body));
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
        return ApiResponse.success(authService.login(body));
    }

    @Operation(summary = "User refresh")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponseDto> refresh(@Valid @RequestBody RefreshRequestDto body) {
        return ApiResponse.success(authService.refresh(body));
    }

    @Operation(summary = "User logout")
    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest request) {
        return ApiResponse.success(authService.logout(AuthContext.requireUser(request)));
    }
}
