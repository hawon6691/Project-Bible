package com.projectbible.shop.admin.controller;

import com.projectbible.shop.admin.dto.AdminDtos.*;
import com.projectbible.shop.admin.service.AdminService;
import com.projectbible.shop.category.dto.CategoryDtos.CategoryResponseDto;
import com.projectbible.shop.category.dto.CategoryDtos.UpsertCategoryDto;
import com.projectbible.shop.category.service.CategoryQueryService;
import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.order.dto.OrderDtos.AdminOrderSummaryDto;
import com.projectbible.shop.order.dto.OrderDtos.OrderDetailDto;
import com.projectbible.shop.order.dto.OrderDtos.OrderStatusResponseDto;
import com.projectbible.shop.order.dto.OrderDtos.UpdateOrderStatusDto;
import com.projectbible.shop.order.service.OrderService;
import com.projectbible.shop.product.dto.ProductDtos.*;
import com.projectbible.shop.product.service.ProductQueryService;
import com.projectbible.shop.review.dto.ReviewDtos.AdminReviewSummaryDto;
import com.projectbible.shop.review.dto.ReviewDtos.ReviewDetailDto;
import com.projectbible.shop.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ApiResponse<java.util.List<AdminOrderSummaryDto>> orders(
        HttpServletRequest request,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status
    ) {
        AuthContext.requireAdmin(request);
        OrderService.PagedAdminOrders result = orderService.adminList(search, status, page, limit);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Admin order detail")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDetailDto> order(HttpServletRequest request, @PathVariable long orderId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(orderService.one(orderId, null));
    }

    @Operation(summary = "Admin update order status")
    @PatchMapping("/orders/{orderId}/status")
    public ApiResponse<OrderStatusResponseDto> orderStatus(HttpServletRequest request, @PathVariable long orderId, @RequestBody UpdateOrderStatusDto body) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(orderService.adminSetStatus(orderId, body));
    }

    @Operation(summary = "Admin list reviews")
    @GetMapping("/reviews")
    public ApiResponse<java.util.List<AdminReviewSummaryDto>> reviews(
        HttpServletRequest request,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status
    ) {
        AuthContext.requireAdmin(request);
        ReviewService.PagedAdminReviews result = reviewService.adminList(search, status, page, limit);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Admin review detail")
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<ReviewDetailDto> review(HttpServletRequest request, @PathVariable long reviewId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(reviewService.one(reviewId));
    }

    @Operation(summary = "Admin delete review")
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<MessageResponse> deleteReview(HttpServletRequest request, @PathVariable long reviewId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(reviewService.adminRemove(reviewId));
    }
}
