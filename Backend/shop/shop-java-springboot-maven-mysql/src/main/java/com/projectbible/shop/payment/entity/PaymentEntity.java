package com.projectbible.shop.payment.entity;

import com.projectbible.shop.order.entity.OrderEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PaymentEntity() {}

    public PaymentEntity(OrderEntity order, String paymentMethod, BigDecimal paidAmount) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "PAID";
        this.paidAmount = paidAmount;
        this.paidAt = LocalDateTime.now();
        this.createdAt = this.paidAt;
        this.updatedAt = this.paidAt;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }

    public void refund() {
        LocalDateTime now = LocalDateTime.now();
        this.paymentStatus = "REFUNDED";
        this.refundedAt = now;
        this.updatedAt = now;
    }
}
