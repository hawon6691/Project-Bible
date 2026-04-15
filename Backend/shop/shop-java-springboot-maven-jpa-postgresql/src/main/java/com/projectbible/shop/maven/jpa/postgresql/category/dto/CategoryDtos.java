package com.projectbible.shop.maven.jpa.postgresql.category.dto;

import java.time.LocalDateTime;

public final class CategoryDtos {
    private CategoryDtos() {}

    public record CategoryResponseDto(Long id, String name, int displayOrder, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record UpsertCategoryDto(String name, Integer displayOrder, String status) {}
}
