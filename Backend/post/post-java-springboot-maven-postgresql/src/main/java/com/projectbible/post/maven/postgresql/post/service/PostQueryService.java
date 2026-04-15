package com.projectbible.post.maven.postgresql.post.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.maven.postgresql.board.entity.BoardEntity;
import com.projectbible.post.maven.postgresql.board.repository.BoardRepository;
import com.projectbible.post.maven.postgresql.common.api.MessageResponse;
import com.projectbible.post.maven.postgresql.common.api.PageMeta;
import com.projectbible.post.maven.postgresql.common.exception.AppException;
import com.projectbible.post.maven.postgresql.common.security.CurrentActor;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.AuthorDto;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.CreatePostDto;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.PostDetailDto;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.PostListItemDto;
import com.projectbible.post.maven.postgresql.post.dto.PostDtos.UpdatePostDto;
import com.projectbible.post.maven.postgresql.post.entity.PostEntity;
import com.projectbible.post.maven.postgresql.post.repository.PostRepository;
import com.projectbible.post.maven.postgresql.user.entity.UserEntity;
import com.projectbible.post.maven.postgresql.user.repository.UserRepository;

@Service
@Transactional
public class PostQueryService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public PostQueryService(PostRepository postRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedPosts list(Integer page, Integer limit, Long boardId, String search, String sort, String status) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = postRepository.countAll(boardId, search, status, false);
        List<PostListItemDto> items = postRepository.findAll(safePage, safeLimit, boardId, search, sort, status, false)
            .stream()
            .map(this::toListItem)
            .toList();
        return new PagedPosts(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    public PostDetailDto one(long postId, boolean incrementView) {
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        if (incrementView) {
            post.incrementView();
            postRepository.save(post);
        }
        return toDetail(post);
    }

    public PostDetailDto create(CurrentActor actor, CreatePostDto body) {
        requireUser(actor);
        BoardEntity board = boardRepository.findActiveById(body.boardId())
            .orElseThrow(() -> new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND));
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        PostEntity post = new PostEntity(board, user, body.title().trim(), body.content().trim());
        postRepository.save(post);
        return toDetail(post);
    }

    public PostDetailDto update(CurrentActor actor, long postId, UpdatePostDto body) {
        requireUser(actor);
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        if (!post.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "Only the author can update this post", HttpStatus.FORBIDDEN);
        }
        post.update(
            body.title() == null || body.title().isBlank() ? post.getTitle() : body.title().trim(),
            body.content() == null || body.content().isBlank() ? post.getContent() : body.content().trim()
        );
        postRepository.save(post);
        return toDetail(post);
    }

    public MessageResponse remove(CurrentActor actor, long postId) {
        requireUser(actor);
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        if (!post.getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "Only the author can delete this post", HttpStatus.FORBIDDEN);
        }
        post.setStatus("DELETED");
        postRepository.save(post);
        return new MessageResponse("Post deleted successfully");
    }

    @Transactional(readOnly = true)
    public PagedPosts adminList(Integer page, Integer limit, Long boardId, String search, String status) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = postRepository.countAll(boardId, search, status, true);
        List<PostListItemDto> items = postRepository.findAll(safePage, safeLimit, boardId, search, "latest", status, true)
            .stream()
            .map(this::toListItem)
            .toList();
        return new PagedPosts(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    public com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.StatusResponseDto adminSetStatus(long postId, String status) {
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        String upper = normalizeStatus(status);
        post.setStatus(upper);
        postRepository.save(post);
        return new com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.StatusResponseDto(post.getId(), post.getStatus(), post.getUpdatedAt());
    }

    private PostListItemDto toListItem(PostEntity post) {
        return new PostListItemDto(
            post.getId(),
            post.getBoard().getId(),
            post.getBoard().getName(),
            post.getTitle(),
            post.getViewCount(),
            post.getLikeCount(),
            post.getCommentCount(),
            post.getStatus(),
            post.getCreatedAt(),
            new AuthorDto(post.getUser().getId(), post.getUser().getNickname())
        );
    }

    private PostDetailDto toDetail(PostEntity post) {
        return new PostDetailDto(
            post.getId(),
            post.getBoard().getId(),
            post.getBoard().getName(),
            post.getTitle(),
            post.getContent(),
            post.getViewCount(),
            post.getLikeCount(),
            post.getCommentCount(),
            post.getStatus(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            new AuthorDto(post.getUser().getId(), post.getUser().getNickname())
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

    public record PagedPosts(List<PostListItemDto> items, PageMeta meta) {}
}
