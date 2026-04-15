package com.projectbible.post.maven.jpa.postgresql.like.controller;

import com.projectbible.post.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.post.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.post.maven.jpa.postgresql.like.dto.LikeDtos.LikeSummaryDto;
import com.projectbible.post.maven.jpa.postgresql.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "likes")
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class LikeController {
    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "Like post")
    @PostMapping
    public ApiResponse<LikeSummaryDto> like(HttpServletRequest request, @PathVariable long postId) {
        return ApiResponse.success(likeService.like(AuthContext.requireUser(request), postId));
    }

    @Operation(summary = "Unlike post")
    @DeleteMapping
    public ApiResponse<LikeSummaryDto> unlike(HttpServletRequest request, @PathVariable long postId) {
        return ApiResponse.success(likeService.unlike(AuthContext.requireUser(request), postId));
    }
}
