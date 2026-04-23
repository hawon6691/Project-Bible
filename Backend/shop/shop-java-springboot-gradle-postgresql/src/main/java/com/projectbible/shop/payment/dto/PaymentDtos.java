package com.projectbible.shop.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record CreatePaymentDto(Long orderId, String paymentMethod) {}
    public record PaymentResponseDto(Long id, Long orderId, String paymentMethod, String paymentStatus, BigDecimal paidAmount, LocalDateTime paidAt, LocalDateTime refundedAt) {}
    public record PaymentRefundResponseDto(Long id, String paymentStatus, LocalDateTime refundedAt) {}
}
