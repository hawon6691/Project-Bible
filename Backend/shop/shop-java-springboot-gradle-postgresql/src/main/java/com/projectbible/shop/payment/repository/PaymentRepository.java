package com.projectbible.shop.payment.repository;

import com.projectbible.shop.payment.entity.PaymentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public PaymentEntity save(PaymentEntity payment) {
        if (payment.getId() == null) {
            entityManager.persist(payment);
            return payment;
        }
        return entityManager.merge(payment);
    }

    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return entityManager.createQuery(
                "select p from PaymentEntity p join fetch p.order o join fetch o.user where o.id = :orderId",
                PaymentEntity.class
            )
            .setParameter("orderId", orderId)
            .getResultList()
            .stream()
            .findFirst();
    }

    public Optional<PaymentEntity> findById(Long paymentId) {
        return entityManager.createQuery(
                "select p from PaymentEntity p join fetch p.order o join fetch o.user where p.id = :paymentId",
                PaymentEntity.class
            )
            .setParameter("paymentId", paymentId)
            .getResultList()
            .stream()
            .findFirst();
    }
}
