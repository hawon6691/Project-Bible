package com.projectbible.post.maven.postgresql.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_refresh_tokens")
public class AdminRefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminEntity admin;

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

    protected AdminRefreshTokenEntity() {
    }

    public AdminRefreshTokenEntity(AdminEntity admin, String tokenKey, LocalDateTime expiresAt) {
        this.admin = admin;
        this.tokenKey = tokenKey;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public AdminEntity getAdmin() { return admin; }
    public String getTokenKey() { return tokenKey; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }

    public void revoke() {
        this.revoked = true;
        this.updatedAt = LocalDateTime.now();
    }
}
