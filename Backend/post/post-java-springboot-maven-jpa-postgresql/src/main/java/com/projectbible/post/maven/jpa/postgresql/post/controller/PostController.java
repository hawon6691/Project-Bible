package com.projectbible.post.maven.jpa.postgresql.post.controller;

import com.projectbible.post.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.post.maven.jpa.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.post.maven.jpa.postgresql.post.dto.PostDtos.CreatePostDto;
import com.projectbible.post.maven.jpa.postgresql.post.dto.PostDtos.PostDetailDto;
import com.projectbible.post.maven.jpa.postgresql.post.dto.PostDtos.PostListItemDto;
import com.projectbible.post.maven.jpa.postgresql.post.dto.PostDtos.UpdatePostDto;
import com.projectbible.post.maven.jpa.postgresql.post.service.PostQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "posts")
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostQueryService service;

    public PostController(PostQueryService service) {
        this.service = service;
    }

    @Operation(summary = "List posts")
    @GetMapping
    public ApiResponse<List<PostListItemDto>> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long boardId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String status
    ) {
        PostQueryService.PagedPosts result = service.list(page, limit, boardId, search, sort, status);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Get post detail")
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailDto> one(@PathVariable long postId) {
        return ApiResponse.success(service.one(postId, true));
    }

    @Operation(summary = "Create post")
    @PostMapping
    public ApiResponse<PostDetailDto> create(HttpServletRequest request, @Valid @RequestBody CreatePostDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Update post")
    @PatchMapping("/{postId}")
    public ApiResponse<PostDetailDto> update(HttpServletRequest request, @PathVariable long postId, @RequestBody UpdatePostDto body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), postId, body));
    }

    @Operation(summary = "Delete post")
    @DeleteMapping("/{postId}")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request, @PathVariable long postId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), postId));
    }
}
