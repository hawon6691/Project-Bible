package com.projectbible.shop.category.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected CategoryEntity() {}

    public CategoryEntity(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getDisplayOrder() { return displayOrder; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, int displayOrder, String status) {
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.status = "DELETED";
        this.updatedAt = LocalDateTime.now();
    }
}
