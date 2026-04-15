package com.projectbible.shop.maven.jpa.postgresql.product.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    @Column(name = "is_primary", nullable = false)
    private boolean primary;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProductImageEntity() {}

    public ProductImageEntity(ProductEntity product, String imageUrl, boolean primary, int displayOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.primary = primary;
        this.displayOrder = displayOrder;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public ProductEntity getProduct() { return product; }
    public String getImageUrl() { return imageUrl; }
    public boolean isPrimary() { return primary; }
    public int getDisplayOrder() { return displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String imageUrl, boolean primary, int displayOrder) {
        this.imageUrl = imageUrl;
        this.primary = primary;
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }
}
