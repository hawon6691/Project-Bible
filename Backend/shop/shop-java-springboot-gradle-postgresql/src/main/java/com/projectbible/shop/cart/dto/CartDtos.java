package com.projectbible.shop.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class CartDtos {
    private CartDtos() {}

    public record CartItemResponseDto(
        Long id,
        Long productId,
        Long productOptionId,
        String productName,
        String optionName,
        BigDecimal unitPrice,
        int quantity,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record UpsertCartItemDto(
        Long productId,
        Long productOptionId,
        Integer quantity
    ) {}
}
