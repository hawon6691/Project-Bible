package com.projectbible.shop.review.controller;

import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.review.dto.ReviewDtos.ReviewDetailDto;
import com.projectbible.shop.review.dto.ReviewDtos.ReviewListItemDto;
import com.projectbible.shop.review.dto.ReviewDtos.UpsertReviewDto;
import com.projectbible.shop.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "reviews")
@RestController
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @Operation(summary = "List product reviews")
    @GetMapping("/api/v1/products/{productId}/reviews")
    public ApiResponse<List<ReviewListItemDto>> list(
        @PathVariable long productId,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        ReviewService.PagedReviews result = service.list(productId, page, limit);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Create review")
    @PostMapping("/api/v1/order-items/{orderItemId}/reviews")
    public ApiResponse<ReviewDetailDto> create(HttpServletRequest request, @PathVariable long orderItemId, @RequestBody UpsertReviewDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), orderItemId, body));
    }

    @Operation(summary = "Update review")
    @PatchMapping("/api/v1/reviews/{reviewId}")
    public ApiResponse<ReviewDetailDto> update(HttpServletRequest request, @PathVariable long reviewId, @RequestBody UpsertReviewDto body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), reviewId, body));
    }

    @Operation(summary = "Delete review")
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request, @PathVariable long reviewId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), reviewId));
    }
}
