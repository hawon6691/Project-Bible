package com.projectbible.post.auth.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.projectbible.post.auth.entity.UserRefreshTokenEntity;
import com.projectbible.post.user.entity.UserEntity;

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

    public Optional<UserEntity> findUserById(Long id) {
        List<UserEntity> users = entityManager.createQuery(
                "select u from UserEntity u where u.id = :id and u.deletedAt is null",
                UserEntity.class
            )
            .setParameter("id", id)
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
        List<UserRefreshTokenEntity> rows = entityManager.createQuery(
                """
                select t from UserRefreshTokenEntity t
                join fetch t.user u
                where t.tokenKey = :tokenKey
                  and t.revoked = false
                  and t.expiresAt > :now
                  and u.deletedAt is null
                """,
                UserRefreshTokenEntity.class
            )
            .setParameter("tokenKey", tokenKey)
            .setParameter("now", LocalDateTime.now())
            .getResultList();
        return rows.stream().findFirst();
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
