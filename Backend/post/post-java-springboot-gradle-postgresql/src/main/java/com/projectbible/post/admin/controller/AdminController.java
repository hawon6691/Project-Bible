package com.projectbible.post.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.projectbible.post.admin.dto.AdminDtos.*;
import com.projectbible.post.admin.service.AdminService;
import com.projectbible.post.board.dto.BoardDtos.BoardResponseDto;
import com.projectbible.post.board.dto.BoardDtos.UpsertBoardDto;
import com.projectbible.post.board.service.BoardQueryService;
import com.projectbible.post.comment.dto.CommentDtos.CommentResponseDto;
import com.projectbible.post.comment.service.CommentService;
import com.projectbible.post.common.api.ApiResponse;
import com.projectbible.post.common.api.MessageResponse;
import com.projectbible.post.common.security.AuthContext;
import com.projectbible.post.post.dto.PostDtos.PostDetailDto;
import com.projectbible.post.post.dto.PostDtos.PostListItemDto;
import com.projectbible.post.post.service.PostQueryService;

@Tag(name = "admin")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService service;
    private final BoardQueryService boardService;
    private final PostQueryService postService;
    private final CommentService commentService;

    public AdminController(AdminService service, BoardQueryService boardService, PostQueryService postService, CommentService commentService) {
        this.service = service;
        this.boardService = boardService;
        this.postService = postService;
        this.commentService = commentService;
    }

    @Operation(summary = "Admin login")
    @PostMapping("/auth/login")
    public ApiResponse<AdminLoginResponseDto> login(@Valid @RequestBody AdminLoginRequestDto body) {
        return ApiResponse.success(service.login(body));
    }

    @Operation(summary = "Admin refresh")
    @PostMapping("/auth/refresh")
    public ApiResponse<AdminLoginResponseDto> refresh(@Valid @RequestBody AdminRefreshRequestDto body) {
        return ApiResponse.success(service.refresh(body));
    }

    @Operation(summary = "Admin logout")
    @PostMapping("/auth/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest request) {
        return ApiResponse.success(service.logout(AuthContext.requireAdmin(request)));
    }

    @Operation(summary = "Current admin")
    @GetMapping("/me")
    public ApiResponse<AdminSummaryDto> me(HttpServletRequest request) {
        return ApiResponse.success(service.me(AuthContext.requireAdmin(request)));
    }

    @Operation(summary = "Admin dashboard")
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponseDto> dashboard(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(service.dashboard());
    }

    @Operation(summary = "Create board")
    @PostMapping("/boards")
    public ApiResponse<BoardResponseDto> createBoard(HttpServletRequest request, @Valid @RequestBody UpsertBoardDto body) {
        return ApiResponse.success(boardService.create(body, AuthContext.requireAdmin(request).id()));
    }

    @Operation(summary = "Update board")
    @PatchMapping("/boards/{boardId}")
    public ApiResponse<BoardResponseDto> updateBoard(HttpServletRequest request, @PathVariable long boardId, @RequestBody UpsertBoardDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(boardService.update(boardId, body));
    }

    @Operation(summary = "Delete board")
    @DeleteMapping("/boards/{boardId}")
    public ApiResponse<MessageResponse> deleteBoard(HttpServletRequest request, @PathVariable long boardId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(boardService.remove(boardId));
    }

    @Operation(summary = "Admin list posts")
    @GetMapping("/posts")
    public ApiResponse<List<PostListItemDto>> posts(
        HttpServletRequest request,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long boardId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status
    ) {
        AuthContext.requireAdmin(request);
        PostQueryService.PagedPosts result = postService.adminList(page, limit, boardId, search, status);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Admin post detail")
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailDto> post(HttpServletRequest request, @PathVariable long postId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(postService.one(postId, false));
    }

    @Operation(summary = "Admin update post status")
    @PatchMapping("/posts/{postId}/status")
    public ApiResponse<StatusResponseDto> postStatus(HttpServletRequest request, @PathVariable long postId, @Valid @RequestBody StatusUpdateDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(postService.adminSetStatus(postId, body.status()));
    }

    @Operation(summary = "Admin list comments")
    @GetMapping("/comments")
    public ApiResponse<List<CommentResponseDto>> comments(
        HttpServletRequest request,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long postId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status
    ) {
        AuthContext.requireAdmin(request);
        CommentService.PagedComments result = commentService.adminList(page, limit, postId, search, status);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Admin comment detail")
    @GetMapping("/comments/{commentId}")
    public ApiResponse<CommentResponseDto> comment(HttpServletRequest request, @PathVariable long commentId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(commentService.one(commentId));
    }

    @Operation(summary = "Admin update comment status")
    @PatchMapping("/comments/{commentId}/status")
    public ApiResponse<StatusResponseDto> commentStatus(HttpServletRequest request, @PathVariable long commentId, @Valid @RequestBody StatusUpdateDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(commentService.adminSetStatus(commentId, body.status()));
    }
}
