package com.projectbible.shop.maven.jpa.postgresql.admin.controller;

import com.projectbible.shop.maven.jpa.postgresql.admin.dto.AdminDtos.*;
import com.projectbible.shop.maven.jpa.postgresql.admin.service.AdminService;
import com.projectbible.shop.maven.jpa.postgresql.category.dto.CategoryDtos.CategoryResponseDto;
import com.projectbible.shop.maven.jpa.postgresql.category.dto.CategoryDtos.UpsertCategoryDto;
import com.projectbible.shop.maven.jpa.postgresql.category.service.CategoryQueryService;
import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.api.MessageResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.shop.maven.jpa.postgresql.order.application.OrderService;
import com.projectbible.shop.maven.jpa.postgresql.product.dto.ProductDtos.*;
import com.projectbible.shop.maven.jpa.postgresql.product.service.ProductQueryService;
import com.projectbible.shop.maven.jpa.postgresql.review.application.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@Tag(name = "admin")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService service;
    private final CategoryQueryService categoryService;
    private final ProductQueryService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;

    public AdminController(AdminService service, CategoryQueryService categoryService, ProductQueryService productService, OrderService orderService, ReviewService reviewService) {
        this.service = service;
        this.categoryService = categoryService;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }

    @Operation(summary = "Admin login")
    @PostMapping("/auth/login")
    public ApiResponse<AdminLoginResponseDto> login(@Valid @RequestBody AdminLoginRequestDto body) {
        return ApiResponse.success(service.login(body));
    }

    @Operation(summary = "Admin refresh")
    @PostMapping("/auth/refresh")
    public ApiResponse<AdminLoginResponseDto> refresh(@Valid @RequestBody AdminRefreshRequestDto body) {
        return ApiResponse.success(service.refresh(body));
    }

    @Operation(summary = "Admin logout")
    @PostMapping("/auth/logout")
    public ApiResponse<MessageResponse> logout(HttpServletRequest request) {
        return ApiResponse.success(service.logout(AuthContext.requireAdmin(request)));
    }

    @Operation(summary = "Current admin")
    @GetMapping("/me")
    public ApiResponse<AdminSummaryDto> me(HttpServletRequest request) {
        return ApiResponse.success(service.me(AuthContext.requireAdmin(request)));
    }

    @Operation(summary = "Admin dashboard")
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponseDto> dashboard(HttpServletRequest request) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(service.dashboard());
    }

    @Operation(summary = "Create category")
    @PostMapping("/categories")
    public ApiResponse<CategoryResponseDto> createCategory(HttpServletRequest request, @RequestBody UpsertCategoryDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(categoryService.create(body));
    }

    @Operation(summary = "Update category")
    @PatchMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponseDto> updateCategory(HttpServletRequest request, @PathVariable long categoryId, @RequestBody UpsertCategoryDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(categoryService.update(categoryId, body));
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<MessageResponse> deleteCategory(HttpServletRequest request, @PathVariable long categoryId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(categoryService.remove(categoryId));
    }

    @Operation(summary = "Create product")
    @PostMapping("/products")
    public ApiResponse<ProductDetailDto> createProduct(HttpServletRequest request, @RequestBody UpsertProductDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.create(body));
    }

    @Operation(summary = "Update product")
    @PatchMapping("/products/{productId}")
    public ApiResponse<ProductDetailDto> updateProduct(HttpServletRequest request, @PathVariable long productId, @RequestBody UpsertProductDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.update(productId, body));
    }

    @Operation(summary = "Delete product")
    @DeleteMapping("/products/{productId}")
    public ApiResponse<MessageResponse> deleteProduct(HttpServletRequest request, @PathVariable long productId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.remove(productId));
    }

    @Operation(summary = "Create product option")
    @PostMapping("/products/{productId}/options")
    public ApiResponse<ProductOptionDto> createOption(HttpServletRequest request, @PathVariable long productId, @RequestBody UpsertProductOptionDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.createOption(productId, body));
    }

    @Operation(summary = "Update product option")
    @PatchMapping("/product-options/{optionId}")
    public ApiResponse<ProductOptionDto> updateOption(HttpServletRequest request, @PathVariable long optionId, @RequestBody UpsertProductOptionDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.updateOption(optionId, body));
    }

    @Operation(summary = "Delete product option")
    @DeleteMapping("/product-options/{optionId}")
    public ApiResponse<MessageResponse> deleteOption(HttpServletRequest request, @PathVariable long optionId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.removeOption(optionId));
    }

    @Operation(summary = "Create product image")
    @PostMapping("/products/{productId}/images")
    public ApiResponse<ProductImageDto> createImage(HttpServletRequest request, @PathVariable long productId, @RequestBody UpsertProductImageDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.createImage(productId, body));
    }

    @Operation(summary = "Update product image")
    @PatchMapping("/product-images/{imageId}")
    public ApiResponse<ProductImageDto> updateImage(HttpServletRequest request, @PathVariable long imageId, @RequestBody UpsertProductImageDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.updateImage(imageId, body));
    }

    @Operation(summary = "Delete product image")
    @DeleteMapping("/product-images/{imageId}")
    public ApiResponse<MessageResponse> deleteImage(HttpServletRequest request, @PathVariable long imageId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(productService.removeImage(imageId));
    }

    @Operation(summary = "Admin list orders")
    @GetMapping("/orders")
    public ApiResponse<Object> orders(HttpServletRequest request, @RequestParam Map<String, String> query) {
        AuthContext.requireAdmin(request);
        Map<String, Object> result = orderService.adminList(query);
        return ApiResponse.success(result.get("items"), result.get("meta"));
    }

    @Operation(summary = "Admin order detail")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<Map<String, Object>> order(HttpServletRequest request, @PathVariable long orderId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(orderService.one(orderId, null));
    }

    @Operation(summary = "Admin update order status")
    @PatchMapping("/orders/{orderId}/status")
    public ApiResponse<Map<String, Object>> orderStatus(HttpServletRequest request, @PathVariable long orderId, @RequestBody Map<String, Object> body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(orderService.adminSetStatus(orderId, String.valueOf(body.get("status"))));
    }

    @Operation(summary = "Admin list reviews")
    @GetMapping("/reviews")
    public ApiResponse<Object> reviews(HttpServletRequest request, @RequestParam Map<String, String> query) {
        AuthContext.requireAdmin(request);
        Map<String, Object> result = reviewService.adminList(query);
        return ApiResponse.success(result.get("items"), result.get("meta"));
    }

    @Operation(summary = "Admin review detail")
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> review(HttpServletRequest request, @PathVariable long reviewId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(reviewService.one(reviewId));
    }

    @Operation(summary = "Admin delete review")
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> deleteReview(HttpServletRequest request, @PathVariable long reviewId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(reviewService.adminRemove(reviewId));
    }
}
