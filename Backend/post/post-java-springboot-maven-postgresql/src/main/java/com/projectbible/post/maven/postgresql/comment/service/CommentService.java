package com.projectbible.post.maven.postgresql.comment.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.CommentResponseDto;
import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.CreateCommentDto;
import com.projectbible.post.maven.postgresql.comment.dto.CommentDtos.UpdateCommentDto;
import com.projectbible.post.maven.postgresql.comment.entity.CommentEntity;
import com.projectbible.post.maven.postgresql.comment.repository.CommentRepository;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.api.PageMeta;
import com.projectbible.post.maven.postgresql.common.exception.AppException;
import com.projectbible.post.maven.postgresql.common.security.CurrentActor;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.AuthorDto;
import com.projectbible.post.maven.postgresql.post.entity.PostEntity;
import com.projectbible.post.maven.postgresql.post.repository.PostRepository;
import com.projectbible.post.maven.postgresql.user.entity.UserEntity;
import com.projectbible.post.maven.postgresql.user.repository.UserRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedComments list(long postId, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = commentRepository.countByPost(postId);
        List<CommentResponseDto> items = commentRepository.findByPost(postId, safePage, safeLimit).stream().map(this::toResponse).toList();
        return new PagedComments(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    @Transactional(readOnly = true)
    public CommentResponseDto one(long commentId) {
        CommentEntity comment = commentRepository.findDetail(commentId)
            .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND));
        return toResponse(comment);
    }

    public CommentResponseDto create(CurrentActor actor, long postId, CreateCommentDto body) {
        requireUser(actor);
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        if (!"ACTIVE".equals(post.getStatus())) {
            throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
        }
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        CommentEntity comment = new CommentEntity(post, user, body.content().trim());
        post.incrementComment();
        commentRepository.save(comment);
        postRepository.save(post);
        return toResponse(comment);
    }

    public CommentResponseDto update(CurrentActor actor, long commentId, UpdateCommentDto body) {
        requireUser(actor);
        CommentEntity comment = commentRepository.findDetail(commentId)
            .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND));
        if (!comment.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "Only the author can update this comment", HttpStatus.FORBIDDEN);
        }
        comment.updateContent(body.content().trim());
        commentRepository.save(comment);
        return toResponse(comment);
    }

    public MessageResponse remove(CurrentActor actor, long commentId) {
        requireUser(actor);
        CommentEntity comment = commentRepository.findDetail(commentId)
            .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND));
        if (!comment.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "Only the author can delete this comment", HttpStatus.FORBIDDEN);
        }
        if (!"DELETED".equals(comment.getStatus())) {
            comment.getPost().decrementComment();
            postRepository.save(comment.getPost());
        }
        comment.setStatus("DELETED");
        commentRepository.save(comment);
        return new MessageResponse("Comment deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedComments adminList(Integer page, Integer limit, Long postId, String search, String status) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = commentRepository.countAll(postId, search, status);
        List<CommentResponseDto> items = commentRepository.findAll(safePage, safeLimit, postId, search, status).stream().map(this::toResponse).toList();
        return new PagedComments(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    public com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.StatusResponseDto adminSetStatus(long commentId, String status) {
        CommentEntity comment = commentRepository.findDetail(commentId)
            .orElseThrow(() -> new AppException("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND));
        String upper = normalizeStatus(status);
        boolean wasDeleted = "DELETED".equals(comment.getStatus());
        comment.setStatus(upper);
        if ("DELETED".equals(upper) && !wasDeleted) {
            comment.getPost().decrementComment();
            postRepository.save(comment.getPost());
        }
        commentRepository.save(comment);
        return new com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.StatusResponseDto(comment.getId(), comment.getStatus(), comment.getUpdatedAt());
    }

    private CommentResponseDto toResponse(CommentEntity comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getPost().getId(),
            comment.getUser().getId(),
            comment.getContent(),
            comment.getStatus(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            new AuthorDto(comment.getUser().getId(), comment.getUser().getNickname())
        );
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    private String normalizeStatus(String status) {
        String upper = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("ACTIVE", "HIDDEN", "DELETED").contains(upper)) {
            throw new AppException("INVALID_STATUS", "Invalid status", HttpStatus.BAD_REQUEST);
        }
        return upper;
    }

    public record PagedComments(List<CommentResponseDto> items, PageMeta meta) {}
}
