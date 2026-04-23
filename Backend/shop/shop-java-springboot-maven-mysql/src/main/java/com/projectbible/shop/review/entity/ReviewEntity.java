package com.projectbible.shop.review.entity;

import com.projectbible.shop.order.entity.OrderItemEntity;
import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.user.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItemEntity orderItem;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(nullable = false)
    private Integer rating;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected ReviewEntity() {}

    public ReviewEntity(OrderItemEntity orderItem, ProductEntity product, UserEntity user, Integer rating, String content) {
        this.orderItem = orderItem;
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.content = content;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public OrderItemEntity getOrderItem() { return orderItem; }
    public ProductEntity getProduct() { return product; }
    public UserEntity getUser() { return user; }
    public Integer getRating() { return rating; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        LocalDateTime now = LocalDateTime.now();
        this.status = "DELETED";
        this.deletedAt = now;
        this.updatedAt = now;
    }
}
