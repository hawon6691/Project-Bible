package com.projectbible.shop.auth.controller;

import com.projectbible.shop.auth.dto.AuthDtos.LoginRequestDto;
import com.projectbible.shop.auth.dto.AuthDtos.LoginResponseDto;
import com.projectbible.shop.auth.dto.AuthDtos.RefreshRequestDto;
import com.projectbible.shop.auth.dto.AuthDtos.SignupRequestDto;
import com.projectbible.shop.auth.service.AuthService;
import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.user.dto.UserDtos.UserSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "auth")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "User signup")
    @PostMapping("/signup")
    public ApiResponse<UserSummaryDto> signup(@Valid @RequestBody SignupRequestDto body) {
        return ApiResponse.success(service.signup(body));
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto body) {
        return ApiResponse.success(service.login(body));
    }

    @Operation(summary = "User refresh")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponseDto> refresh(@Valid @RequestBody RefreshRequestDto body) {
        return ApiResponse.success(service.refresh(body));
    }

    @Operation(summary = "User logout")
    @PostMapping("/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest request) {
        return ApiResponse.success(service.logout(AuthContext.requireUser(request)));
    }
}
