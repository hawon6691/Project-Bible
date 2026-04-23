package com.projectbible.shop.address.repository;

import com.projectbible.shop.address.entity.AddressEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AddressRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<AddressEntity> findAllByUserId(Long userId) {
        return entityManager.createQuery(
                "select a from AddressEntity a join fetch a.user where a.user.id = :userId order by a.isDefault desc, a.id asc",
                AddressEntity.class
            )
            .setParameter("userId", userId)
            .getResultList();
    }

    public Optional<AddressEntity> findByIdAndUserId(Long addressId, Long userId) {
        List<AddressEntity> rows = entityManager.createQuery(
                "select a from AddressEntity a join fetch a.user where a.id = :addressId and a.user.id = :userId",
                AddressEntity.class
            )
            .setParameter("addressId", addressId)
            .setParameter("userId", userId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public void clearDefaultByUserId(Long userId) {
        entityManager.createQuery(
                "update AddressEntity a set a.isDefault = false, a.updatedAt = current_timestamp where a.user.id = :userId and a.isDefault = true"
            )
            .setParameter("userId", userId)
            .executeUpdate();
    }

    public AddressEntity save(AddressEntity address) {
        if (address.getId() == null) {
            entityManager.persist(address);
            return address;
        }
        return entityManager.merge(address);
    }

    public void remove(AddressEntity address) {
        entityManager.remove(entityManager.contains(address) ? address : entityManager.merge(address));
    }
}
