package com.projectbible.shop.maven.jpa.postgresql.category.repository;

import com.projectbible.shop.maven.jpa.postgresql.category.entity.CategoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<CategoryEntity> findAllVisible() {
        return entityManager.createQuery(
                "select c from CategoryEntity c where c.status <> 'DELETED' order by c.displayOrder asc, c.id asc",
                CategoryEntity.class
            )
            .getResultList();
    }

    public List<CategoryEntity> findByStatus(String status) {
        return entityManager.createQuery(
                "select c from CategoryEntity c where c.status = :status order by c.displayOrder asc, c.id asc",
                CategoryEntity.class
            )
            .setParameter("status", status)
            .getResultList();
    }

    public Optional<CategoryEntity> findById(Long categoryId) {
        return Optional.ofNullable(entityManager.find(CategoryEntity.class, categoryId));
    }

    public Optional<CategoryEntity> findUsableById(Long categoryId) {
        List<CategoryEntity> rows = entityManager.createQuery(
                "select c from CategoryEntity c where c.id = :categoryId and c.status <> 'DELETED'",
                CategoryEntity.class
            )
            .setParameter("categoryId", categoryId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public CategoryEntity save(CategoryEntity category) {
        if (category.getId() == null) {
            entityManager.persist(category);
            return category;
        }
        return entityManager.merge(category);
    }
}
