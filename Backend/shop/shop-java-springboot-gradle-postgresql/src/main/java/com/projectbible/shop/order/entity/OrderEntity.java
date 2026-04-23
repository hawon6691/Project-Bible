package com.projectbible.shop.order.entity;

import com.projectbible.shop.user.entity.UserEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;
    @Column(name = "order_status", nullable = false)
    private String orderStatus;
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;
    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected OrderEntity() {}

    public OrderEntity(UserEntity user, String orderNumber, BigDecimal totalAmount) {
        this.user = user;
        this.orderNumber = orderNumber;
        this.orderStatus = "PENDING";
        this.totalAmount = totalAmount;
        this.paymentStatus = "READY";
        this.orderedAt = LocalDateTime.now();
        this.createdAt = this.orderedAt;
        this.updatedAt = this.orderedAt;
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getOrderNumber() { return orderNumber; }
    public String getOrderStatus() { return orderStatus; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void markCancelled() {
        LocalDateTime now = LocalDateTime.now();
        this.orderStatus = "CANCELLED";
        this.cancelledAt = now;
        this.updatedAt = now;
    }

    public void updateOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
