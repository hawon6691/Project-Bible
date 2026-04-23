package com.projectbible.shop.cart.entity;

import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import com.projectbible.shop.user.entity.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOptionEntity productOption;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CartItemEntity() {}

    public CartItemEntity(UserEntity user, ProductEntity product, ProductOptionEntity productOption, int quantity) {
        this.user = user;
        this.product = product;
        this.productOption = productOption;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public ProductEntity getProduct() { return product; }
    public ProductOptionEntity getProductOption() { return productOption; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }
}
