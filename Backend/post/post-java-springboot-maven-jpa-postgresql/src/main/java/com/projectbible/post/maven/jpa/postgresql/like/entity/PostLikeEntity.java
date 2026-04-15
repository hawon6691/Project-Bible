package com.projectbible.post.maven.jpa.postgresql.like.entity;

import com.projectbible.post.maven.jpa.postgresql.post.entity.PostEntity;
import com.projectbible.post.maven.jpa.postgresql.user.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostLikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PostLikeEntity() {
    }

    public PostLikeEntity(PostEntity post, UserEntity user) {
        this.post = post;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }
}
