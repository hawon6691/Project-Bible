package com.projectbible.post.maven.postgresql.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.CommentResponseDto;
import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.CreateCommentDto;
import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.UpdateCommentDto;
import com.projectbible.post.maven.postgresql.comment.service.CommentService;
import com.projectbible.post.maven.postgresql.common.api.ApiResponse;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.security.AuthContext;

@Tag(name = "comments")
@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "List comments")
    @GetMapping("/api/v1/posts/{postId}/comments")
    public ApiResponse<List<CommentResponseDto>> list(
        @PathVariable long postId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        CommentService.PagedComments result = commentService.list(postId, page, limit);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Create comment")
    @PostMapping("/api/v1/posts/{postId}/comments")
    public ApiResponse<CommentResponseDto> create(HttpServletRequest request, @PathVariable long postId, @Valid @RequestBody CreateCommentDto body) {
        return ApiResponse.success(commentService.create(AuthContext.requireUser(request), postId, body));
    }

    @Operation(summary = "Update comment")
    @PatchMapping("/api/v1/comments/{commentId}")
    public ApiResponse<CommentResponseDto> update(HttpServletRequest request, @PathVariable long commentId, @Valid @RequestBody UpdateCommentDto body) {
        return ApiResponse.success(commentService.update(AuthContext.requireUser(request), commentId, body));
    }

    @Operation(summary = "Delete comment")
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request, @PathVariable long commentId) {
        return ApiResponse.success(commentService.remove(AuthContext.requireUser(request), commentId));
    }
}
