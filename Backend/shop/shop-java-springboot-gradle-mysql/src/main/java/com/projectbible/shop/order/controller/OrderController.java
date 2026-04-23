package com.projectbible.shop.order.controller;

import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.order.dto.OrderDtos.CreateOrderDto;
import com.projectbible.shop.order.dto.OrderDtos.OrderDetailDto;
import com.projectbible.shop.order.dto.OrderDtos.OrderSummaryDto;
import com.projectbible.shop.order.dto.OrderDtos.OrderStatusResponseDto;
import com.projectbible.shop.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "orders")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Create order")
    @PostMapping
    public ApiResponse<OrderDetailDto> create(HttpServletRequest request, @RequestBody CreateOrderDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "List my orders")
    @GetMapping
    public ApiResponse<List<OrderSummaryDto>> list(
        HttpServletRequest request,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit
    ) {
        OrderService.PagedOrders result = service.list(AuthContext.requireUser(request), status, page, limit);
        return ApiResponse.success(result.items(), result.meta());
    }

    @Operation(summary = "Get my order detail")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailDto> one(HttpServletRequest request, @PathVariable long orderId) {
        return ApiResponse.success(service.one(orderId, AuthContext.requireUser(request)));
    }

    @Operation(summary = "Cancel order")
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderStatusResponseDto> cancel(HttpServletRequest request, @PathVariable long orderId) {
        return ApiResponse.success(service.cancel(AuthContext.requireUser(request), orderId));
    }
}
