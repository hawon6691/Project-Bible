package com.projectbible.post.maven.postgresql.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.projectbible.post.maven.postgresql.user.entity.UserEntity;

@Entity
@Table(name = "user_refresh_tokens")
public class UserRefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token_key", nullable = false)
    private String tokenKey;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserRefreshTokenEntity() {
    }

    public UserRefreshTokenEntity(UserEntity user, String tokenKey, LocalDateTime expiresAt) {
        this.user = user;
        this.tokenKey = tokenKey;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getTokenKey() { return tokenKey; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }

    public void revoke() {
        this.revoked = true;
        this.updatedAt = LocalDateTime.now();
    }
}
