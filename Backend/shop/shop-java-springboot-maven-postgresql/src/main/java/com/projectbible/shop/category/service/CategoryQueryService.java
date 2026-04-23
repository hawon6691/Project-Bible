package com.projectbible.shop.category.service;

import com.projectbible.shop.category.dto.CategoryDtos.CategoryResponseDto;
import com.projectbible.shop.category.dto.CategoryDtos.UpsertCategoryDto;
import com.projectbible.shop.category.entity.CategoryEntity;
import com.projectbible.shop.category.repository.CategoryRepository;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.exception.AppException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryQueryService {
    private final CategoryRepository categoryRepository;

    public CategoryQueryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> list(String status) {
        List<CategoryEntity> categories = status == null || status.isBlank()
            ? categoryRepository.findAllVisible()
            : categoryRepository.findByStatus(status.trim().toUpperCase());
        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto one(long categoryId) {
        CategoryEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
        return toResponse(category);
    }

    public CategoryResponseDto create(UpsertCategoryDto body) {
        CategoryEntity category = new CategoryEntity(body.name().trim(), body.displayOrder() == null ? 0 : body.displayOrder());
        categoryRepository.save(category);
        return toResponse(category);
    }

    public CategoryResponseDto update(long categoryId, UpsertCategoryDto body) {
        CategoryEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
        category.update(
            body.name() == null || body.name().isBlank() ? category.getName() : body.name().trim(),
            body.displayOrder() == null ? category.getDisplayOrder() : body.displayOrder(),
            body.status() == null || body.status().isBlank() ? category.getStatus() : body.status().trim().toUpperCase()
        );
        categoryRepository.save(category);
        return toResponse(category);
    }

    public MessageResponse remove(long categoryId) {
        CategoryEntity category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND));
        category.markDeleted();
        categoryRepository.save(category);
        return new MessageResponse("Category deleted successfully");
    }

    private CategoryResponseDto toResponse(CategoryEntity category) {
        return new CategoryResponseDto(category.getId(), category.getName(), category.getDisplayOrder(), category.getStatus(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
