package com.projectbible.post.maven.postgresql.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.projectbible.post.maven.postgresql.admin.dto.AdminDtos.AdminDashboardResponseDto;
import com.projectbible.post.maven.postgresql.admin.entity.AdminEntity;
import com.projectbible.post.maven.postgresql.admin.entity.AdminRefreshTokenEntity;

@Repository
public class AdminRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Optional<AdminEntity> findByEmail(String email) {
        List<AdminEntity> admins = entityManager.createQuery(
                "select a from AdminEntity a where lower(a.email) = lower(:email)",
                AdminEntity.class
            )
            .setParameter("email", email)
            .getResultList();
        return admins.stream().findFirst();
    }

    public Optional<AdminEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(AdminEntity.class, id));
    }

    public AdminRefreshTokenEntity saveRefreshToken(AdminRefreshTokenEntity token) {
        if (token.getId() == null) {
            entityManager.persist(token);
            return token;
        }
        return entityManager.merge(token);
    }

    public Optional<AdminRefreshTokenEntity> findActiveRefreshToken(String tokenKey) {
        List<AdminRefreshTokenEntity> tokens = entityManager.createQuery(
                """
                select t from AdminRefreshTokenEntity t
                join fetch t.admin a
                where t.tokenKey = :tokenKey and t.revoked = false and t.expiresAt > :now
                """,
                AdminRefreshTokenEntity.class
            )
            .setParameter("tokenKey", tokenKey)
            .setParameter("now", LocalDateTime.now())
            .getResultList();
        return tokens.stream().findFirst();
    }

    public void revokeAllAdminTokens(Long adminId) {
        entityManager.createQuery(
                "update AdminRefreshTokenEntity t set t.revoked = true, t.updatedAt = :now where t.admin.id = :adminId and t.revoked = false"
            )
            .setParameter("now", LocalDateTime.now())
            .setParameter("adminId", adminId)
            .executeUpdate();
    }

    public AdminDashboardResponseDto dashboard() {
        int boardCount = entityManager.createQuery("select count(b) from BoardEntity b", Long.class).getSingleResult().intValue();
        int postCount = entityManager.createQuery("select count(p) from PostEntity p", Long.class).getSingleResult().intValue();
        int commentCount = entityManager.createQuery("select count(c) from CommentEntity c", Long.class).getSingleResult().intValue();
        int hiddenPostCount = entityManager.createQuery("select count(p) from PostEntity p where p.status = 'HIDDEN'", Long.class).getSingleResult().intValue();
        int hiddenCommentCount = entityManager.createQuery("select count(c) from CommentEntity c where c.status = 'HIDDEN'", Long.class).getSingleResult().intValue();
        return new AdminDashboardResponseDto(boardCount, postCount, commentCount, hiddenPostCount, hiddenCommentCount);
    }
}
