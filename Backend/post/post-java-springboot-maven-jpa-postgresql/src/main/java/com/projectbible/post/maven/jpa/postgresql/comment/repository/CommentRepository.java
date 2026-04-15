package com.projectbible.post.maven.jpa.postgresql.comment.repository;

import com.projectbible.post.maven.jpa.postgresql.comment.entity.CommentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<CommentEntity> findByPost(Long postId, int page, int limit) {
        return entityManager.createQuery(
                """
                select c from CommentEntity c
                join fetch c.user
                join fetch c.post
                where c.post.id = :postId and c.status <> 'DELETED'
                order by c.createdAt asc
                """,
                CommentEntity.class
            )
            .setParameter("postId", postId)
            .setFirstResult((page - 1) * limit)
            .setMaxResults(limit)
            .getResultList();
    }

    public long countByPost(Long postId) {
        return entityManager.createQuery(
                "select count(c) from CommentEntity c where c.post.id = :postId and c.status <> 'DELETED'",
                Long.class
            )
            .setParameter("postId", postId)
            .getSingleResult();
    }

    public Optional<CommentEntity> findDetail(Long commentId) {
        List<CommentEntity> rows = entityManager.createQuery(
                """
                select c from CommentEntity c
                join fetch c.user
                join fetch c.post
                where c.id = :commentId and c.status <> 'DELETED'
                """,
                CommentEntity.class
            )
            .setParameter("commentId", commentId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public List<CommentEntity> findAll(int page, int limit, Long postId, String search, String status) {
        StringBuilder jpql = new StringBuilder("select c from CommentEntity c join fetch c.user u join fetch c.post p where 1 = 1");
        if (postId != null) {
            jpql.append(" and p.id = :postId");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(c.content) like :pattern or lower(u.nickname) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and c.status = :status");
        }
        jpql.append(" order by c.createdAt desc");
        TypedQuery<CommentEntity> query = entityManager.createQuery(jpql.toString(), CommentEntity.class);
        if (postId != null) query.setParameter("postId", postId);
        if (search != null && !search.isBlank()) query.setParameter("pattern", "%" + search.trim().toLowerCase() + "%");
        if (status != null && !status.isBlank()) query.setParameter("status", status.trim().toUpperCase());
        return query.setFirstResult((page - 1) * limit).setMaxResults(limit).getResultList();
    }

    public long countAll(Long postId, String search, String status) {
        StringBuilder jpql = new StringBuilder("select count(c) from CommentEntity c join c.user u join c.post p where 1 = 1");
        if (postId != null) {
            jpql.append(" and p.id = :postId");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(c.content) like :pattern or lower(u.nickname) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and c.status = :status");
        }
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        if (postId != null) query.setParameter("postId", postId);
        if (search != null && !search.isBlank()) query.setParameter("pattern", "%" + search.trim().toLowerCase() + "%");
        if (status != null && !status.isBlank()) query.setParameter("status", status.trim().toUpperCase());
        return query.getSingleResult();
    }

    public CommentEntity save(CommentEntity comment) {
        if (comment.getId() == null) {
            entityManager.persist(comment);
            return comment;
        }
        return entityManager.merge(comment);
    }
}
