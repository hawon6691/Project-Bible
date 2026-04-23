package com.projectbible.shop.cart.controller;

import com.projectbible.shop.cart.dto.CartDtos.CartItemResponseDto;
import com.projectbible.shop.cart.dto.CartDtos.UpsertCartItemDto;
import com.projectbible.shop.cart.service.CartService;
import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.api.MessageResponse;
import com.projectbible.shop.common.security.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
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
    public ApiResponse<List<CartItemResponseDto>> list(HttpServletRequest request) {
        return ApiResponse.success(service.list(AuthContext.requireUser(request)));
    }

    @Operation(summary = "Add cart item")
    @PostMapping
    public ApiResponse<CartItemResponseDto> create(HttpServletRequest request, @RequestBody UpsertCartItemDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Update cart item")
    @PatchMapping("/{cartItemId}")
    public ApiResponse<CartItemResponseDto> update(HttpServletRequest request, @PathVariable long cartItemId, @RequestBody UpsertCartItemDto body) {
        return ApiResponse.success(service.update(AuthContext.requireUser(request), cartItemId, body));
    }

    @Operation(summary = "Delete cart item")
    @DeleteMapping("/{cartItemId}")
    public ApiResponse<MessageResponse> remove(HttpServletRequest request, @PathVariable long cartItemId) {
        return ApiResponse.success(service.remove(AuthContext.requireUser(request), cartItemId));
    }

    @Operation(summary = "Clear cart")
    @DeleteMapping
    public ApiResponse<MessageResponse> clear(HttpServletRequest request) {
        return ApiResponse.success(service.clear(AuthContext.requireUser(request)));
    }
}
