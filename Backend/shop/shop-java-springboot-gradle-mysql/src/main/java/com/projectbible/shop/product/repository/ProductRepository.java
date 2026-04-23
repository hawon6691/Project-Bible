package com.projectbible.shop.product.repository;

import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductImageEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ProductEntity> findAll(int page, int limit, Long categoryId, String search, String sort, String status, boolean admin) {
        StringBuilder jpql = new StringBuilder("select p from ProductEntity p join fetch p.category c where ");
        if (admin) {
            jpql.append("1 = 1");
        } else {
            jpql.append("p.status <> 'DELETED' and p.status = 'ACTIVE'");
        }
        if (categoryId != null) jpql.append(" and c.id = :categoryId");
        if (search != null && !search.isBlank()) jpql.append(" and (lower(p.name) like :pattern or lower(coalesce(p.description, '')) like :pattern)");
        if (status != null && !status.isBlank()) {
            if (!admin) jpql = new StringBuilder(jpql.toString().replace("p.status = 'ACTIVE'", "1 = 1"));
            jpql.append(" and p.status = :status");
        }
        jpql.append(" order by ").append(sortExpression(sort, admin));
        TypedQuery<ProductEntity> query = entityManager.createQuery(jpql.toString(), ProductEntity.class);
        bindFilters(query, categoryId, search, status);
        return query.setFirstResult((page - 1) * limit).setMaxResults(limit).getResultList();
    }

    public long countAll(Long categoryId, String search, String status, boolean admin) {
        StringBuilder jpql = new StringBuilder("select count(p) from ProductEntity p join p.category c where ");
        if (admin) {
            jpql.append("1 = 1");
        } else {
            jpql.append("p.status <> 'DELETED' and p.status = 'ACTIVE'");
        }
        if (categoryId != null) jpql.append(" and c.id = :categoryId");
        if (search != null && !search.isBlank()) jpql.append(" and (lower(p.name) like :pattern or lower(coalesce(p.description, '')) like :pattern)");
        if (status != null && !status.isBlank()) {
            if (!admin) jpql = new StringBuilder(jpql.toString().replace("p.status = 'ACTIVE'", "1 = 1"));
            jpql.append(" and p.status = :status");
        }
        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        bindFilters(query, categoryId, search, status);
        return query.getSingleResult();
    }

    public Optional<ProductEntity> findDetail(Long productId) {
        List<ProductEntity> rows = entityManager.createQuery(
                "select p from ProductEntity p join fetch p.category where p.id = :productId and p.status <> 'DELETED'",
                ProductEntity.class
            )
            .setParameter("productId", productId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public List<ProductOptionEntity> findOptions(Long productId) {
        return entityManager.createQuery(
                "select o from ProductOptionEntity o join fetch o.product where o.product.id = :productId order by o.id asc",
                ProductOptionEntity.class
            )
            .setParameter("productId", productId)
            .getResultList();
    }

    public List<ProductImageEntity> findImages(Long productId) {
        return entityManager.createQuery(
                "select i from ProductImageEntity i join fetch i.product where i.product.id = :productId order by i.displayOrder asc, i.id asc",
                ProductImageEntity.class
            )
            .setParameter("productId", productId)
            .getResultList();
    }

    public Optional<ProductOptionEntity> findOption(Long optionId) {
        return Optional.ofNullable(entityManager.find(ProductOptionEntity.class, optionId));
    }

    public Optional<ProductImageEntity> findImage(Long imageId) {
        return Optional.ofNullable(entityManager.find(ProductImageEntity.class, imageId));
    }

    public ProductEntity save(ProductEntity product) {
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        }
        return entityManager.merge(product);
    }

    public ProductOptionEntity saveOption(ProductOptionEntity option) {
        if (option.getId() == null) {
            entityManager.persist(option);
            return option;
        }
        return entityManager.merge(option);
    }

    public ProductImageEntity saveImage(ProductImageEntity image) {
        if (image.getId() == null) {
            entityManager.persist(image);
            return image;
        }
        return entityManager.merge(image);
    }

    public void removeOption(ProductOptionEntity option) {
        entityManager.remove(entityManager.contains(option) ? option : entityManager.merge(option));
    }

    public void removeImage(ProductImageEntity image) {
        entityManager.remove(entityManager.contains(image) ? image : entityManager.merge(image));
    }

    private void bindFilters(TypedQuery<?> query, Long categoryId, String search, String status) {
        if (categoryId != null) query.setParameter("categoryId", categoryId);
        if (search != null && !search.isBlank()) query.setParameter("pattern", "%" + search.trim().toLowerCase() + "%");
        if (status != null && !status.isBlank()) query.setParameter("status", status.trim().toUpperCase());
    }

    private String sortExpression(String sort, boolean admin) {
        if (sort == null) return "p.createdAt desc, p.id desc";
        if (admin) return "p.createdAt desc, p.id desc";
        return switch (sort.toLowerCase()) {
            case "price_asc" -> "p.price asc, p.id desc";
            case "price_desc" -> "p.price desc, p.id desc";
            case "popular" -> "p.stock desc, p.id desc";
            default -> "p.createdAt desc, p.id desc";
        };
    }
}
