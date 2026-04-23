package com.projectbible.shop.auth.repository;

import com.projectbible.shop.auth.entity.UserRefreshTokenEntity;
import com.projectbible.shop.user.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public boolean existsUserByEmail(String email) {
        Long count = entityManager.createQuery(
                "select count(u) from UserEntity u where lower(u.email) = lower(:email) and u.deletedAt is null",
                Long.class
            )
            .setParameter("email", email)
            .getSingleResult();
        return count > 0;
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        List<UserEntity> users = entityManager.createQuery(
                "select u from UserEntity u where lower(u.email) = lower(:email) and u.deletedAt is null",
                UserEntity.class
            )
            .setParameter("email", email)
            .getResultList();
        return users.stream().findFirst();
    }

    public UserEntity saveUser(UserEntity user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        return entityManager.merge(user);
    }

    public UserRefreshTokenEntity saveRefreshToken(UserRefreshTokenEntity token) {
        if (token.getId() == null) {
            entityManager.persist(token);
            return token;
        }
        return entityManager.merge(token);
    }

    public Optional<UserRefreshTokenEntity> findActiveRefreshToken(String tokenKey) {
        List<UserRefreshTokenEntity> tokens = entityManager.createQuery(
                """
                select t from UserRefreshTokenEntity t
                join fetch t.user u
                where t.tokenKey = :tokenKey and t.revoked = false and t.expiresAt > :now and u.deletedAt is null
                """,
                UserRefreshTokenEntity.class
            )
            .setParameter("tokenKey", tokenKey)
            .setParameter("now", LocalDateTime.now())
            .getResultList();
        return tokens.stream().findFirst();
    }

    public void revokeAllUserTokens(Long userId) {
        entityManager.createQuery(
                "update UserRefreshTokenEntity t set t.revoked = true, t.updatedAt = :now where t.user.id = :userId and t.revoked = false"
            )
            .setParameter("now", LocalDateTime.now())
            .setParameter("userId", userId)
            .executeUpdate();
    }
}
