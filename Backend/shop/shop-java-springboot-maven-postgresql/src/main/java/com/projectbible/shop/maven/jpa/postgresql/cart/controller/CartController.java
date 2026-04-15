package com.projectbible.shop.maven.jpa.postgresql.cart.presentation;

import com.projectbible.shop.maven.jpa.postgresql.cart.application.CartService;
import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@Tag(name = "cart")
@RestController
@RequestMapping("/api/v1/cart-items")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @Operation(summary = "List cart items")
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(HttpServletRequest request) {
        return ApiResponse.success(service.list(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Add cart item")
    @PostMapping
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Update cart item")
    @PatchMapping("/{cartItemId}")
    public ApiResponse<Map<String, Object>> update(HttpServletRequest request, @PathVariable long cartItemId, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), cartItemId, body));
    }

    @Operation(summary = "Delete cart item")
    @DeleteMapping("/{cartItemId}")
    public ApiResponse<Map<String, Object>> remove(HttpServletRequest request, @PathVariable long cartItemId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), cartItemId));
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping
    public ApiResponse<Map<String, Object>> clear(HttpServletRequest request) {
        return ApiResponse.success(service.clear(AuthContext.requireUser(request)));
    }
}
