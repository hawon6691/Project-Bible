package com.projectbible.shop.user.controller;

import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.user.dto.UserDtos.UpdateUserDto;
import com.projectbible.shop.user.dto.UserDtos.UserSummaryDto;
import com.projectbible.shop.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "users")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Current user")
    @GetMapping("/me")
    public ApiResponse<UserSummaryDto> me(HttpServletRequest request) {
        return ApiResponse.success(service.me(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Update current user")
    @PatchMapping("/me")
    public ApiResponse<UserSummaryDto> update(HttpServletRequest request, @RequestBody UpdateUserDto body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Delete current user")
    @DeleteMapping("/me")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request)));
    }
}
