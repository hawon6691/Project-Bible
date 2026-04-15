package com.projectbible.shop.maven.jpa.postgresql.admin.repository;

import com.projectbible.shop.maven.jpa.postgresql.admin.dto.AdminDtos.AdminDashboardResponseDto;
import com.projectbible.shop.maven.jpa.postgresql.admin.entity.AdminEntity;
import com.projectbible.shop.maven.jpa.postgresql.admin.entity.AdminRefreshTokenEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

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
        int categoryCount = entityManager.createQuery("select count(c) from CategoryEntity c", Long.class).getSingleResult().intValue();
        int productCount = entityManager.createQuery("select count(p) from ProductEntity p", Long.class).getSingleResult().intValue();
        int userCount = entityManager.createQuery("select count(u) from UserEntity u where u.deletedAt is null", Long.class).getSingleResult().intValue();
        int reviewCount = ((Number) entityManager.createNativeQuery("select count(*) from reviews").getSingleResult()).intValue();
        int pendingOrderCount = ((Number) entityManager.createNativeQuery("select count(*) from orders where order_status = 'PENDING'").getSingleResult()).intValue();
        return new AdminDashboardResponseDto(categoryCount, productCount, userCount, reviewCount, pendingOrderCount);
    }
}
