package com.projectbible.post.comment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.projectbible.post.post.entity.PostEntity;
import com.projectbible.post.user.entity.UserEntity;

@Entity
@Table(name = "comments")
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected CommentEntity() {
    }

    public CommentEntity(PostEntity post, UserEntity user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public PostEntity getPost() { return post; }
    public UserEntity getUser() { return user; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(String status) {
        this.status = status;
        if ("DELETED".equals(status)) {
            this.deletedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}
