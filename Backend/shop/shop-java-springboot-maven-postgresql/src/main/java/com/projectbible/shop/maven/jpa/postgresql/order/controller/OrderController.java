package com.projectbible.shop.maven.jpa.postgresql.order.presentation;

import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.shop.maven.jpa.postgresql.order.application.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
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
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "List my orders")
    @GetMapping
    public ApiResponse<Object> list(HttpServletRequest request, @RequestParam Map<String, String> query) {
        Map<String, Object> result = service.list(AuthContext.requireUser(request), query);
        return ApiResponse.success(result.get("items"), result.get("meta"));
    }

    @Operation(summary = "Get my order detail")
    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> one(HttpServletRequest request, @PathVariable long orderId) {
        return ApiResponse.success(service.one(orderId, AuthContext.requireUser(request)));
    }

    @Operation(summary = "Cancel order")
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Map<String, Object>> cancel(HttpServletRequest request, @PathVariable long orderId) {
        return ApiResponse.success(service.cancel(AuthContext.requireUser(request), orderId));
    }
}
