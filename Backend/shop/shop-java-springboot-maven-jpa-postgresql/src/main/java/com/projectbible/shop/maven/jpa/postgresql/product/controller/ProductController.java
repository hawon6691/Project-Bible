package com.projectbible.shop.maven.jpa.postgresql.product.controller;

import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.product.dto.ProductDtos.ProductDetailDto;
import com.projectbible.shop.maven.jpa.postgresql.product.dto.ProductDtos.ProductListItemDto;
import com.projectbible.shop.maven.jpa.postgresql.product.service.ProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "products")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductQueryService service;

    public ProductController(ProductQueryService service) {
        this.service = service;
    }

    @Operation(summary = "List products")
    @GetMapping
    public ApiResponse<List<ProductListItemDto>> list(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String status
    ) {
        ProductQueryService.PagedProducts result = service.list(page, limit, categoryId, search, sort, status);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Get product detail")
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailDto> one(@PathVariable long productId) {
        return ApiResponse.success(service.one(productId));
    }
}
