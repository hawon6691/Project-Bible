package com.projectbible.shop.product.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_options")
public class ProductOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String value;
    @Column(name = "additional_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal additionalPrice;
    @Column(nullable = false)
    private int stock;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProductOptionEntity() {}

    public ProductOptionEntity(ProductEntity product, String name, String value, BigDecimal additionalPrice, int stock) {
        this.product = product;
        this.name = name;
        this.value = value;
        this.additionalPrice = additionalPrice;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public ProductEntity getProduct() { return product; }
    public String getName() { return name; }
    public String getValue() { return value; }
    public BigDecimal getAdditionalPrice() { return additionalPrice; }
    public int getStock() { return stock; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String value, BigDecimal additionalPrice, int stock) {
        this.name = name;
        this.value = value;
        this.additionalPrice = additionalPrice;
        this.stock = stock;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseStock(int quantity) {
        this.stock -= quantity;
        this.updatedAt = LocalDateTime.now();
    }
}
