package com.projectbible.shop.maven.jpa.postgresql.category.controller;

import com.projectbible.shop.maven.jpa.postgresql.category.dto.CategoryDtos.CategoryResponseDto;
import com.projectbible.shop.maven.jpa.postgresql.category.service.CategoryQueryService;
import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryQueryService service;

    public CategoryController(CategoryQueryService service) {
        this.service = service;
    }

    @Operation(summary = "List categories")
    @GetMapping
    public ApiResponse<List<CategoryResponseDto>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(status));
    }

    @Operation(summary = "Get category detail")
    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDto> one(@PathVariable long categoryId) {
        return ApiResponse.success(service.one(categoryId));
    }
}
