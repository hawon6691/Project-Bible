package com.projectbible.shop.cart.repository;

import com.projectbible.shop.cart.entity.CartItemEntity;
import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<CartItemEntity> findAllByUserId(Long userId) {
        return entityManager.createQuery(
                """
                select c from CartItemEntity c
                join fetch c.user
                join fetch c.product p
                left join fetch c.productOption po
                where c.user.id = :userId and p.status <> 'DELETED'
                order by c.id asc
                """,
                CartItemEntity.class
            )
            .setParameter("userId", userId)
            .getResultList();
    }

    public Optional<CartItemEntity> findByIdAndUserId(Long cartItemId, Long userId) {
        List<CartItemEntity> rows = entityManager.createQuery(
                """
                select c from CartItemEntity c
                join fetch c.user
                join fetch c.product
                left join fetch c.productOption
                where c.id = :cartItemId and c.user.id = :userId
                """,
                CartItemEntity.class
            )
            .setParameter("cartItemId", cartItemId)
            .setParameter("userId", userId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public Optional<CartItemEntity> findDuplicate(Long userId, Long productId, Long productOptionId) {
        String jpql = """
            select c from CartItemEntity c
            join fetch c.user
            join fetch c.product
            left join fetch c.productOption
            where c.user.id = :userId and c.product.id = :productId
            """;
        if (productOptionId == null) {
            jpql += " and c.productOption is null";
            return entityManager.createQuery(jpql, CartItemEntity.class)
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .getResultList()
                .stream()
                .findFirst();
        }
        jpql += " and c.productOption.id = :productOptionId";
        return entityManager.createQuery(jpql, CartItemEntity.class)
            .setParameter("userId", userId)
            .setParameter("productId", productId)
            .setParameter("productOptionId", productOptionId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<ProductEntity> findUsableProduct(Long productId) {
        return entityManager.createQuery(
                "select p from ProductEntity p join fetch p.category where p.id = :productId and p.status = 'ACTIVE' and p.deletedAt is null",
                ProductEntity.class
            )
            .setParameter("productId", productId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<ProductOptionEntity> findOption(Long productId, Long optionId) {
        return entityManager.createQuery(
                "select o from ProductOptionEntity o join fetch o.product where o.id = :optionId and o.product.id = :productId",
                ProductOptionEntity.class
            )
            .setParameter("optionId", optionId)
            .setParameter("productId", productId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public CartItemEntity save(CartItemEntity item) {
        if (item.getId() == null) {
            entityManager.persist(item);
            return item;
        }
        return entityManager.merge(item);
    }

    public void remove(CartItemEntity item) {
        entityManager.remove(entityManager.contains(item) ? item : entityManager.merge(item));
    }

    public int clearByUserId(Long userId) {
        return entityManager.createQuery("delete from CartItemEntity c where c.user.id = :userId")
            .setParameter("userId", userId)
            .executeUpdate();
    }
}
