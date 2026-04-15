package com.projectbible.shop.maven.jpa.postgresql.review.presentation;

import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.shop.maven.jpa.postgresql.review.application.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
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
    public ApiResponse<Object> list(@PathVariable long productId, @RequestParam Map<String, String> query) {
        Map<String, Object> result = service.list(productId, query);
        return ApiResponse.success(result.get("items"), result.get("meta"));
    }

    @Operation(summary = "Create review")
    @PostMapping("/api/v1/order-items/{orderItemId}/reviews")
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @PathVariable long orderItemId, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), orderItemId, body));
    }

    @Operation(summary = "Update review")
    @PatchMapping("/api/v1/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> update(HttpServletRequest request, @PathVariable long reviewId, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), reviewId, body));
    }

    @Operation(summary = "Delete review")
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> remove(HttpServletRequest request, @PathVariable long reviewId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), reviewId));
    }
}
