package com.projectbible.shop.order.dto;

import com.projectbible.shop.payment.dto.PaymentDtos.PaymentResponseDto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderDto(List<Long> cartItemIds, Long addressId) {}
    public record OrderSummaryDto(Long id, String orderNumber, String orderStatus, String paymentStatus, BigDecimal totalAmount, LocalDateTime orderedAt, LocalDateTime cancelledAt) {}
    public record OrderCustomerDto(Long id, String name) {}
    public record AdminOrderSummaryDto(Long id, String orderNumber, String orderStatus, String paymentStatus, BigDecimal totalAmount, LocalDateTime orderedAt, OrderCustomerDto customer) {}
    public record OrderAddressDto(String recipientName, String phone, String zipCode, String address1, String address2) {}
    public record OrderItemDto(Long id, Long productId, Long productOptionId, String productNameSnapshot, String optionNameSnapshot, BigDecimal unitPrice, int quantity, BigDecimal lineAmount) {}
    public record OrderDetailDto(OrderSummaryDto order, OrderAddressDto orderAddress, List<OrderItemDto> orderItems, PaymentResponseDto payment) {}
    public record UpdateOrderStatusDto(String orderStatus) {}
    public record OrderStatusResponseDto(Long id, String orderStatus, LocalDateTime updatedAt) {}
}
