package com.projectbible.shop.maven.jpa.postgresql.payment.presentation;

import com.projectbible.shop.maven.jpa.postgresql.common.api.ApiResponse;
import com.projectbible.shop.maven.jpa.postgresql.common.security.AuthContext;
import com.projectbible.shop.maven.jpa.postgresql.payment.application.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@Tag(name = "payments")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Create payment")
    @PostMapping
    public ApiResponse<Map<String, Object>> create(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Get payment detail")
    @GetMapping("/{paymentId}")
    public ApiResponse<Map<String, Object>> one(HttpServletRequest request, @PathVariable long paymentId) {
        return ApiResponse.success(service.one(paymentId, AuthContext.requireUser(request)));
    }

    @Operation(summary = "Refund payment")
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<Map<String, Object>> refund(HttpServletRequest request, @PathVariable long paymentId) {
        AuthContext.requireAdmin(request);
        return ApiResponse.success(service.refund(paymentId));
    }
}
