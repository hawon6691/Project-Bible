package com.projectbible.shop.review.dto;

import java.time.LocalDateTime;

public final class ReviewDtos {
    private ReviewDtos() {}

    public record UpsertReviewDto(Integer rating, String content) {}
    public record ReviewListItemDto(Long id, Long productId, Long userId, String userName, Integer rating, String content, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record ReviewDetailDto(Long id, Long orderItemId, Long productId, String productName, Long userId, String userName, Integer rating, String content, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record AdminReviewSummaryDto(Long id, Long productId, String productName, Long userId, String userName, Integer rating, String status, LocalDateTime createdAt) {}
}
