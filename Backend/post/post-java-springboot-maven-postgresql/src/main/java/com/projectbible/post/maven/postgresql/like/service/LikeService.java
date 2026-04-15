package com.projectbible.post.maven.postgresql.like.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.maven.postgresql.common.exception.AppException;
import com.projectbible.post.maven.postgresql.common.security.CurrentActor;
import com.projectbible.post.maven.postgresql.like.dto.LikeDtos.LikeSummaryDto;
import com.projectbible.post.maven.postgresql.like.entity.PostLikeEntity;
import com.projectbible.post.maven.postgresql.like.repository.LikeRepository;
import com.projectbible.post.maven.postgresql.post.entity.PostEntity;
import com.projectbible.post.maven.postgresql.post.repository.PostRepository;
import com.projectbible.post.maven.postgresql.user.entity.UserEntity;
import com.projectbible.post.maven.postgresql.user.repository.UserRepository;

@Service
@Transactional
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public LikeSummaryDto like(CurrentActor actor, long postId) {
        requireUser(actor);
        PostEntity post = activePost(postId);
        if (likeRepository.exists(postId, actor.id())) {
            throw new AppException("DUPLICATE_LIKE", "Post already liked", HttpStatus.CONFLICT);
        }
        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        likeRepository.save(new PostLikeEntity(post, user));
        post.incrementLike();
        postRepository.save(post);
        return new LikeSummaryDto(postId, true, post.getLikeCount());
    }

    public LikeSummaryDto unlike(CurrentActor actor, long postId) {
        requireUser(actor);
        PostEntity post = activePost(postId);
        int deleted = likeRepository.delete(postId, actor.id());
        if (deleted > 0) {
            post.decrementLike();
            postRepository.save(post);
        }
        return new LikeSummaryDto(postId, false, post.getLikeCount());
    }

    private PostEntity activePost(long postId) {
        PostEntity post = postRepository.findDetail(postId)
            .orElseThrow(() -> new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND));
        if (!"ACTIVE".equals(post.getStatus())) {
            throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
        }
        return post;
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }
}
