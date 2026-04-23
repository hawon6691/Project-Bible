package com.projectbible.shop.order.entity;

import com.projectbible.shop.product.entity.ProductEntity;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id")
    private ProductOptionEntity productOption;
    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;
    @Column(name = "option_name_snapshot")
    private String optionNameSnapshot;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "line_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineAmount;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected OrderItemEntity() {}

    public OrderItemEntity(OrderEntity order, ProductEntity product, ProductOptionEntity productOption, String productNameSnapshot, String optionNameSnapshot, BigDecimal unitPrice, int quantity, BigDecimal lineAmount) {
        this.order = order;
        this.product = product;
        this.productOption = productOption;
        this.productNameSnapshot = productNameSnapshot;
        this.optionNameSnapshot = optionNameSnapshot;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineAmount = lineAmount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public ProductEntity getProduct() { return product; }
    public ProductOptionEntity getProductOption() { return productOption; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public String getOptionNameSnapshot() { return optionNameSnapshot; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineAmount() { return lineAmount; }
}
