package com.projectbible.post.maven.postgresql.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.projectbible.post.maven.postgresql.common.api.ApiResponse;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.security.AuthContext;
import com.projectbible.post.maven.postgresql.user.dto.UserDtos.UpdateUserDto;
import com.projectbible.post.maven.postgresql.user.dto.UserDtos.UserSummaryDto;
import com.projectbible.post.maven.postgresql.user.service.UserService;

@Tag(name = "users")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Current user")
    @GetMapping("/me")
    public ApiResponse<UserSummaryDto> me(HttpServletRequest request) {
        return ApiResponse.success(userService.me(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Update current user")
    @PatchMapping("/me")
    public ApiResponse<UserSummaryDto> update(HttpServletRequest request, @Valid @RequestBody UpdateUserDto body) {
        return ApiResponse.success(userService.update(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Delete current user")
    @DeleteMapping("/me")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request) {
        return ApiResponse.success(userService.remove(AuthContext.requireUser(request)));
    }
}
