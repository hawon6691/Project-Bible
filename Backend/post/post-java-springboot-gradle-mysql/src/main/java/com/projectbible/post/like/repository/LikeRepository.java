package com.projectbible.post.like.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Repository;

import com.projectbible.post.like.entity.PostLikeEntity;

@Repository
public class LikeRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public boolean exists(Long postId, Long userId) {
        Long count = entityManager.createQuery(
                "select count(l) from PostLikeEntity l where l.post.id = :postId and l.user.id = :userId",
                Long.class
            )
            .setParameter("postId", postId)
            .setParameter("userId", userId)
            .getSingleResult();
        return count > 0;
    }

    public void save(PostLikeEntity like) {
        entityManager.persist(like);
    }

    public int delete(Long postId, Long userId) {
        List<PostLikeEntity> likes = entityManager.createQuery(
                "select l from PostLikeEntity l where l.post.id = :postId and l.user.id = :userId",
                PostLikeEntity.class
            )
            .setParameter("postId", postId)
            .setParameter("userId", userId)
            .getResultList();
        likes.forEach(entityManager::remove);
        return likes.size();
    }
}
