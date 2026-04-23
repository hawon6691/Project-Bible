package com.projectbible.shop.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductDtos {
    private ProductDtos() {}

    public record ProductListItemDto(Long id, Long categoryId, String name, BigDecimal price, int stock, String status, String thumbnailUrl) {}
    public record ProductOptionDto(Long id, Long productId, String name, String value, BigDecimal additionalPrice, int stock, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record ProductImageDto(Long id, Long productId, String imageUrl, boolean isPrimary, int displayOrder, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record ProductDetailDto(Long id, Long categoryId, String name, String description, BigDecimal price, int stock, String status, LocalDateTime createdAt, LocalDateTime updatedAt, List<ProductOptionDto> options, List<ProductImageDto> images) {}
    public record UpsertProductDto(Long categoryId, String name, String description, BigDecimal price, Integer stock, String status) {}
    public record UpsertProductOptionDto(String name, String value, BigDecimal additionalPrice, Integer stock) {}
    public record UpsertProductImageDto(String imageUrl, Boolean isPrimary, Integer displayOrder) {}
}
