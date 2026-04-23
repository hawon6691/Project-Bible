package com.projectbible.post.board.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.projectbible.post.admin.entity.AdminEntity;

@Entity
@Table(name = "boards")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id", nullable = false)
    private AdminEntity createdByAdmin;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BoardEntity() {
    }

    public BoardEntity(AdminEntity createdByAdmin, String name, String description, int displayOrder) {
        this.createdByAdmin = createdByAdmin;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String description, int displayOrder, String status) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.status = "DELETED";
        this.updatedAt = LocalDateTime.now();
    }
}
