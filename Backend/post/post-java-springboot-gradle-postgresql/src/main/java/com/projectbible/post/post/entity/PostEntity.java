package com.projectbible.post.post.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.projectbible.post.board.entity.BoardEntity;
import com.projectbible.post.user.entity.UserEntity;

@Entity
@Table(name = "posts")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected PostEntity() {
    }

    public PostEntity(BoardEntity board, UserEntity user, String title, String content) {
        this.board = board;
        this.user = user;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public BoardEntity getBoard() { return board; }
    public UserEntity getUser() { return user; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getViewCount() { return viewCount; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementView() {
        this.viewCount += 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementLike() {
        this.likeCount += 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementLike() {
        this.likeCount = Math.max(this.likeCount - 1, 0);
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementComment() {
        this.commentCount += 1;
        this.updatedAt = LocalDateTime.now();
    }

    public void decrementComment() {
        this.commentCount = Math.max(this.commentCount - 1, 0);
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
