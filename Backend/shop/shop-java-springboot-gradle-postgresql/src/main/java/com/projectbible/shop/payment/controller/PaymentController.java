package com.projectbible.shop.payment.controller;

import com.projectbible.shop.common.api.ApiResponse;
import com.projectbible.shop.common.security.AuthContext;
import com.projectbible.shop.payment.dto.PaymentDtos.CreatePaymentDto;
import com.projectbible.shop.payment.dto.PaymentDtos.PaymentRefundResponseDto;
import com.projectbible.shop.payment.dto.PaymentDtos.PaymentResponseDto;
import com.projectbible.shop.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResponse<PaymentResponseDto> create(HttpServletRequest request, @RequestBody CreatePaymentDto body) {
        return ApiResponse.success(service.create(AuthContext.requireUser(request), body));
    }

    @Operation(summary = "Get payment detail")
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponseDto> one(HttpServletRequest request, @PathVariable long paymentId) {
        return ApiResponse.success(service.one(paymentId, AuthContext.requireUser(request)));
    }

    @Operation(summary = "Refund payment")
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentRefundResponseDto> refund(HttpServletRequest request, @PathVariable long paymentId) {
        return ApiResponse.success(service.refund(paymentId, AuthContext.requireUser(request)));
    }
}
