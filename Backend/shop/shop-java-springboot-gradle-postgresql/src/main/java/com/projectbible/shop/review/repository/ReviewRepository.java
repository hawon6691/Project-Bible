package com.projectbible.shop.review.repository;

import com.projectbible.shop.order.entity.OrderItemEntity;
import com.projectbible.shop.review.entity.ReviewEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public long countByProductId(Long productId) {
        return entityManager.createQuery(
                "select count(r) from ReviewEntity r where r.product.id = :productId and r.status = 'ACTIVE'",
                Long.class
            )
            .setParameter("productId", productId)
            .getSingleResult();
    }

    public List<ReviewEntity> findByProductId(Long productId, int page, int limit) {
        return entityManager.createQuery(
                """
                select r from ReviewEntity r
                join fetch r.product
                join fetch r.user
                join fetch r.orderItem oi
                where r.product.id = :productId and r.status = 'ACTIVE'
                order by r.createdAt desc
                """,
                ReviewEntity.class
            )
            .setParameter("productId", productId)
            .setFirstResult((page - 1) * limit)
            .setMaxResults(limit)
            .getResultList();
    }

    public Optional<ReviewEntity> findById(Long reviewId) {
        return entityManager.createQuery(
                """
                select r from ReviewEntity r
                join fetch r.product
                join fetch r.user
                join fetch r.orderItem oi
                where r.id = :reviewId
                """,
                ReviewEntity.class
            )
            .setParameter("reviewId", reviewId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<ReviewEntity> findByOrderItemId(Long orderItemId) {
        return entityManager.createQuery(
                "select r from ReviewEntity r join fetch r.orderItem oi where oi.id = :orderItemId",
                ReviewEntity.class
            )
            .setParameter("orderItemId", orderItemId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<OrderItemEntity> findReviewableOrderItem(Long orderItemId, Long userId) {
        return entityManager.createQuery(
                """
                select oi from OrderItemEntity oi
                join fetch oi.order o
                join fetch oi.product
                left join fetch oi.productOption
                where oi.id = :orderItemId and o.user.id = :userId
                """,
                OrderItemEntity.class
            )
            .setParameter("orderItemId", orderItemId)
            .setParameter("userId", userId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public ReviewEntity save(ReviewEntity review) {
        if (review.getId() == null) {
            entityManager.persist(review);
            return review;
        }
        return entityManager.merge(review);
    }

    public long countAdmin(String search, String status) {
        StringBuilder jpql = new StringBuilder("select count(r) from ReviewEntity r join r.user u join r.product p where 1 = 1");
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(u.name) like :pattern or lower(p.name) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and r.status = :status");
        }
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        bindAdminFilters(query, search, status);
        return query.getSingleResult();
    }

    public List<ReviewEntity> findAdmin(int page, int limit, String search, String status) {
        StringBuilder jpql = new StringBuilder(
            "select r from ReviewEntity r join fetch r.user u join fetch r.product p join fetch r.orderItem oi where 1 = 1"
        );
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(u.name) like :pattern or lower(p.name) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and r.status = :status");
        }
        jpql.append(" order by r.createdAt desc");
        TypedQuery<ReviewEntity> query = entityManager.createQuery(jpql.toString(), ReviewEntity.class)
            .setFirstResult((page - 1) * limit)
            .setMaxResults(limit);
        bindAdminFilters(query, search, status);
        return query.getResultList();
    }

    private void bindAdminFilters(TypedQuery<?> query, String search, String status) {
        if (search != null && !search.isBlank()) {
            query.setParameter("pattern", "%" + search.trim().toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) {
            query.setParameter("status", status.trim().toUpperCase());
        }
    }
}
