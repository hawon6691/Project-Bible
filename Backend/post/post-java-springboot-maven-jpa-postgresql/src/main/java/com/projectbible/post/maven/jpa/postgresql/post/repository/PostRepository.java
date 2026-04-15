package com.projectbible.post.maven.jpa.postgresql.post.repository;

import com.projectbible.post.maven.jpa.postgresql.post.entity.PostEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<PostEntity> findAll(int page, int limit, Long boardId, String search, String sort, String status, boolean admin) {
        StringBuilder jpql = new StringBuilder("select p from PostEntity p join fetch p.board b join fetch p.user u where ");
        if (admin) {
            jpql.append("1 = 1");
        } else {
            jpql.append("p.status <> 'DELETED' and p.status = 'ACTIVE'");
        }
        if (boardId != null) {
            jpql.append(" and b.id = :boardId");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(p.title) like :pattern or lower(p.content) like :pattern");
            if (admin) {
                jpql.append(" or lower(u.nickname) like :pattern");
            }
            jpql.append(")");
        }
        if (status != null && !status.isBlank()) {
            if (!admin) {
                jpql = new StringBuilder(jpql.toString().replace("p.status = 'ACTIVE'", "1 = 1"));
            }
            jpql.append(" and p.status = :status");
        }
        jpql.append(" order by ").append(sortExpression(sort, admin));
        TypedQuery<PostEntity> query = entityManager.createQuery(jpql.toString(), PostEntity.class);
        bindFilters(query, boardId, search, status);
        return query.setFirstResult((page - 1) * limit).setMaxResults(limit).getResultList();
    }

    public long countAll(Long boardId, String search, String status, boolean admin) {
        StringBuilder jpql = new StringBuilder("select count(p) from PostEntity p join p.board b join p.user u where ");
        if (admin) {
            jpql.append("1 = 1");
        } else {
            jpql.append("p.status <> 'DELETED' and p.status = 'ACTIVE'");
        }
        if (boardId != null) {
            jpql.append(" and b.id = :boardId");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(p.title) like :pattern or lower(p.content) like :pattern");
            if (admin) {
                jpql.append(" or lower(u.nickname) like :pattern");
            }
            jpql.append(")");
        }
        if (status != null && !status.isBlank()) {
            if (!admin) {
                jpql = new StringBuilder(jpql.toString().replace("p.status = 'ACTIVE'", "1 = 1"));
            }
            jpql.append(" and p.status = :status");
        }
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        bindFilters(query, boardId, search, status);
        return query.getSingleResult();
    }

    public Optional<PostEntity> findDetail(Long postId) {
        List<PostEntity> rows = entityManager.createQuery(
                "select p from PostEntity p join fetch p.board join fetch p.user where p.id = :postId and p.status <> 'DELETED'",
                PostEntity.class
            )
            .setParameter("postId", postId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public PostEntity save(PostEntity post) {
        if (post.getId() == null) {
            entityManager.persist(post);
            return post;
        }
        return entityManager.merge(post);
    }

    private void bindFilters(TypedQuery<?> query, Long boardId, String search, String status) {
        if (boardId != null) {
            query.setParameter("boardId", boardId);
        }
        if (search != null && !search.isBlank()) {
            query.setParameter("pattern", "%" + search.trim().toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status.trim().toUpperCase());
        }
    }

    private String sortExpression(String sort, boolean admin) {
        if (admin) {
            return "p.createdAt desc, p.id desc";
        }
        if (sort == null) {
            return "p.createdAt desc, p.id desc";
        }
        return switch (sort.toLowerCase()) {
            case "view_count" -> "p.viewCount desc, p.id desc";
            case "like_count" -> "p.likeCount desc, p.id desc";
            default -> "p.createdAt desc, p.id desc";
        };
    }
}
