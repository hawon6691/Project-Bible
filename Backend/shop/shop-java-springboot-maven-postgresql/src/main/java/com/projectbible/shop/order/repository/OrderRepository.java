package com.projectbible.shop.order.repository;

import com.projectbible.shop.order.entity.OrderAddressEntity;
import com.projectbible.shop.order.entity.OrderEntity;
import com.projectbible.shop.order.entity.OrderItemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public OrderEntity save(OrderEntity order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        }
        return entityManager.merge(order);
    }

    public OrderAddressEntity saveAddress(OrderAddressEntity address) {
        if (address.getId() == null) {
            entityManager.persist(address);
            return address;
        }
        return entityManager.merge(address);
    }

    public OrderItemEntity saveItem(OrderItemEntity item) {
        if (item.getId() == null) {
            entityManager.persist(item);
            return item;
        }
        return entityManager.merge(item);
    }

    public Optional<OrderEntity> findById(Long orderId) {
        return entityManager.createQuery(
                "select o from OrderEntity o join fetch o.user where o.id = :orderId",
                OrderEntity.class
            )
            .setParameter("orderId", orderId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<OrderEntity> findByIdAndUserId(Long orderId, Long userId) {
        return entityManager.createQuery(
                "select o from OrderEntity o join fetch o.user where o.id = :orderId and o.user.id = :userId",
                OrderEntity.class
            )
            .setParameter("orderId", orderId)
            .setParameter("userId", userId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<OrderAddressEntity> findAddressByOrderId(Long orderId) {
        return entityManager.createQuery(
                "select oa from OrderAddressEntity oa join fetch oa.order where oa.order.id = :orderId",
                OrderAddressEntity.class
            )
            .setParameter("orderId", orderId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public List<OrderItemEntity> findItemsByOrderId(Long orderId) {
        return entityManager.createQuery(
                """
                select oi from OrderItemEntity oi
                join fetch oi.order
                join fetch oi.product
                left join fetch oi.productOption
                where oi.order.id = :orderId
                order by oi.id asc
                """,
                OrderItemEntity.class
            )
            .setParameter("orderId", orderId)
            .getResultList();
    }

    public long countByUserId(Long userId, String status) {
        String jpql = "select count(o) from OrderEntity o where o.user.id = :userId";
        if (status != null && !status.isBlank()) jpql += " and o.orderStatus = :status";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class).setParameter("userId", userId);
        if (status != null && !status.isBlank()) query.setParameter("status", status.trim().toUpperCase());
        return query.getSingleResult();
    }

    public List<OrderEntity> findByUserId(Long userId, int page, int limit, String status) {
        String jpql = "select o from OrderEntity o where o.user.id = :userId";
        if (status != null && !status.isBlank()) jpql += " and o.orderStatus = :status";
        jpql += " order by o.orderedAt desc";
        TypedQuery<OrderEntity> query = entityManager.createQuery(jpql, OrderEntity.class)
            .setParameter("userId", userId)
            .setFirstResult((page - 1) * limit)
            .setMaxResults(limit);
        if (status != null && !status.isBlank()) query.setParameter("status", status.trim().toUpperCase());
        return query.getResultList();
    }

    public long countAdmin(String search, String status) {
        StringBuilder jpql = new StringBuilder("select count(o) from OrderEntity o join o.user u where 1 = 1");
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(o.orderNumber) like :pattern or lower(u.name) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and o.orderStatus = :status");
        }
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        bindAdminFilters(query, search, status);
        return query.getSingleResult();
    }

    public List<OrderEntity> findAdmin(int page, int limit, String search, String status) {
        StringBuilder jpql = new StringBuilder("select o from OrderEntity o join fetch o.user u where 1 = 1");
        if (search != null && !search.isBlank()) {
            jpql.append(" and (lower(o.orderNumber) like :pattern or lower(u.name) like :pattern)");
        }
        if (status != null && !status.isBlank()) {
            jpql.append(" and o.orderStatus = :status");
        }
        jpql.append(" order by o.orderedAt desc");
        TypedQuery<OrderEntity> query = entityManager.createQuery(jpql.toString(), OrderEntity.class)
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
