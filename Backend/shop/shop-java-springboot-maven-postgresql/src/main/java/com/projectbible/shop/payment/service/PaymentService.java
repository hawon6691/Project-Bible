package com.projectbible.shop.payment.service;

import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.order.entity.OrderEntity;
import com.projectbible.shop.order.repository.OrderRepository;
import com.projectbible.shop.payment.dto.PaymentDtos.CreatePaymentDto;
import com.projectbible.shop.payment.dto.PaymentDtos.PaymentRefundResponseDto;
import com.projectbible.shop.payment.dto.PaymentDtos.PaymentResponseDto;
import com.projectbible.shop.payment.entity.PaymentEntity;
import com.projectbible.shop.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponseDto create(CurrentActor actor, CreatePaymentDto body) {
        requireUser(actor);
        if (body == null || body.orderId() == null) {
            throw new AppException("VALIDATION_ERROR", "orderId is required", HttpStatus.BAD_REQUEST);
        }
        if (body.paymentMethod() == null || body.paymentMethod().isBlank()) {
            throw new AppException("VALIDATION_ERROR", "paymentMethod is required", HttpStatus.BAD_REQUEST);
        }
        OrderEntity order = orderRepository.findByIdAndUserId(body.orderId(), actor.id())
            .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Payment is not allowed", HttpStatus.CONFLICT);
        }
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Payment already exists", HttpStatus.CONFLICT);
        }
        PaymentEntity payment = new PaymentEntity(order, body.paymentMethod().trim(), order.getTotalAmount());
        paymentRepository.save(payment);
        order.updatePaymentStatus("PAID");
        order.updateOrderStatus("PAID");
        orderRepository.save(order);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto one(long paymentId, CurrentActor actor) {
        requireUser(actor);
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new AppException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND));
        if (!payment.getOrder().getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        return toResponse(payment);
    }

    public PaymentRefundResponseDto refund(long paymentId, CurrentActor actor) {
        requireUser(actor);
        PaymentEntity payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new AppException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND));
        if (!payment.getOrder().getUser().getId().equals(actor.id())) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        if (!"PAID".equals(payment.getPaymentStatus())) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Refund is not allowed", HttpStatus.CONFLICT);
        }
        payment.refund();
        paymentRepository.save(payment);
        payment.getOrder().updatePaymentStatus("REFUNDED");
        orderRepository.save(payment.getOrder());
        return new PaymentRefundResponseDto(payment.getId(), payment.getPaymentStatus(), payment.getRefundedAt());
    }

    private PaymentResponseDto toResponse(PaymentEntity payment) {
        return new PaymentResponseDto(
            payment.getId(),
            payment.getOrder().getId(),
            payment.getPaymentMethod(),
            payment.getPaymentStatus(),
            payment.getPaidAmount(),
            payment.getPaidAt(),
            payment.getRefundedAt()
        );
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }
}
