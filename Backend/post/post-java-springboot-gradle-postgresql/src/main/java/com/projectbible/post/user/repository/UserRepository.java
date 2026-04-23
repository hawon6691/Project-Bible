package com.projectbible.post.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.projectbible.post.user.entity.UserEntity;

@Repository
public class UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Optional<UserEntity> findActiveById(Long id) {
        List<UserEntity> users = entityManager.createQuery(
                "select u from UserEntity u where u.id = :id and u.deletedAt is null",
                UserEntity.class
            )
            .setParameter("id", id)
            .getResultList();
        return users.stream().findFirst();
    }

    public UserEntity save(UserEntity user) {
        return entityManager.merge(user);
    }
}
