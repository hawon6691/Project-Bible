package com.projectbible.shop.order.service;

import com.projectbible.shop.address.entity.AddressEntity;
import com.projectbible.shop.address.service.AddressService;
import com.projectbible.shop.cart.entity.CartItemEntity;
import com.projectbible.shop.cart.repository.CartRepository;
import com.projectbible.shop.common.api.PageMeta;
import com.projectbible.shop.common.exception.AppException;
import com.projectbible.shop.common.security.CurrentActor;
import com.projectbible.shop.order.dto.OrderDtos.*;
import com.projectbible.shop.order.entity.OrderAddressEntity;
import com.projectbible.shop.order.entity.OrderEntity;
import com.projectbible.shop.order.entity.OrderItemEntity;
import com.projectbible.shop.order.repository.OrderRepository;
import com.projectbible.shop.payment.dto.PaymentDtos.PaymentResponseDto;
import com.projectbible.shop.payment.entity.PaymentEntity;
import com.projectbible.shop.payment.repository.PaymentRepository;
import com.projectbible.shop.product.entity.ProductOptionEntity;
import com.projectbible.shop.user.entity.UserEntity;
import com.projectbible.shop.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressService addressService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, AddressService addressService, UserRepository userRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressService = addressService;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    public OrderDetailDto create(CurrentActor actor, CreateOrderDto body) {
        requireUser(actor);
        if (body == null || body.cartItemIds() == null || body.cartItemIds().isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "cartItemIds is required", HttpStatus.BAD_REQUEST);
        }
        if (body.addressId() == null) {
            throw new AppException("VALIDATION_ERROR", "addressId is required", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = userRepository.findActiveById(actor.id())
            .orElseThrow(() -> new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
        AddressEntity address = addressService.requireOwnedEntity(body.addressId(), actor.id());
        List<CartItemEntity> cartItems = new ArrayList<>();
        for (Long cartItemId : body.cartItemIds()) {
            CartItemEntity item = cartRepository.findByIdAndUserId(cartItemId, actor.id())
                .orElseThrow(() -> new AppException("CART_ITEM_NOT_FOUND", "Some cart items were not found", HttpStatus.NOT_FOUND));
            cartItems.add(item);
        }
        if (cartItems.size() != body.cartItemIds().size()) {
            throw new AppException("CART_ITEM_NOT_FOUND", "Some cart items were not found", HttpStatus.NOT_FOUND);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemEntity cartItem : cartItems) {
            int available = cartItem.getProductOption() == null ? cartItem.getProduct().getStock() : cartItem.getProductOption().getStock();
            if (cartItem.getQuantity() > available) {
                throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
            }
            totalAmount = totalAmount.add(unitPrice(cartItem).multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        OrderEntity order = new OrderEntity(user, orderNumber(), totalAmount);
        orderRepository.save(order);
        orderRepository.saveAddress(new OrderAddressEntity(order, address.getRecipientName(), address.getPhone(), address.getZipCode(), address.getAddress1(), address.getAddress2()));

        for (CartItemEntity cartItem : cartItems) {
            ProductOptionEntity option = cartItem.getProductOption();
            BigDecimal unitPrice = unitPrice(cartItem);
            OrderItemEntity orderItem = new OrderItemEntity(
                order,
                cartItem.getProduct(),
                option,
                cartItem.getProduct().getName(),
                option == null ? null : option.getName() + ":" + option.getValue(),
                unitPrice,
                cartItem.getQuantity(),
                unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
            orderRepository.saveItem(orderItem);
            cartItem.getProduct().decreaseStock(cartItem.getQuantity());
            if (option != null) {
                option.decreaseStock(cartItem.getQuantity());
            }
            cartRepository.remove(cartItem);
        }

        return one(order.getId(), actor);
    }

    @Transactional(readOnly = true)
    public PagedOrders list(CurrentActor actor, String status, Integer page, Integer limit) {
        requireUser(actor);
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = orderRepository.countByUserId(actor.id(), status);
        List<OrderSummaryDto> items = orderRepository.findByUserId(actor.id(), safePage, safeLimit, status)
            .stream()
            .map(this::toSummary)
            .toList();
        return new PagedOrders(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    @Transactional(readOnly = true)
    public OrderDetailDto one(long orderId, CurrentActor actor) {
        OrderEntity order = actor == null
            ? orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND))
            : orderRepository.findByIdAndUserId(orderId, actor.id())
                .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));

        OrderAddressEntity orderAddress = orderRepository.findAddressByOrderId(orderId)
            .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        List<OrderItemDto> items = orderRepository.findItemsByOrderId(orderId).stream().map(this::toItem).toList();
        PaymentResponseDto payment = paymentRepository.findByOrderId(orderId).map(this::toPayment).orElse(null);
        return new OrderDetailDto(toSummary(order), toAddress(orderAddress), items, payment);
    }

    public OrderStatusResponseDto cancel(CurrentActor actor, long orderId) {
        requireUser(actor);
        OrderEntity order = orderRepository.findByIdAndUserId(orderId, actor.id())
            .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        if (List.of("SHIPPING", "DELIVERED", "CANCELLED").contains(order.getOrderStatus())) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Order cannot be cancelled", HttpStatus.CONFLICT);
        }
        order.markCancelled();
        orderRepository.save(order);
        return new OrderStatusResponseDto(order.getId(), order.getOrderStatus(), order.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public PagedAdminOrders adminList(String search, String status, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 20 : limit;
        long totalCount = orderRepository.countAdmin(search, status);
        List<AdminOrderSummaryDto> items = orderRepository.findAdmin(safePage, safeLimit, search, status).stream()
            .map(order -> new AdminOrderSummaryDto(
                order.getId(),
                order.getOrderNumber(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getOrderedAt(),
                new OrderCustomerDto(order.getUser().getId(), order.getUser().getName())
            ))
            .toList();
        return new PagedAdminOrders(items, PageMeta.of(safePage, safeLimit, totalCount));
    }

    public OrderStatusResponseDto adminSetStatus(long orderId, UpdateOrderStatusDto body) {
        String status = body == null || body.orderStatus() == null ? "" : body.orderStatus().trim().toUpperCase();
        if (!List.of("PENDING", "PAID", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED").contains(status)) {
            throw new AppException("INVALID_STATUS", "Invalid order status", HttpStatus.BAD_REQUEST);
        }
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND));
        order.updateOrderStatus(status);
        orderRepository.save(order);
        return new OrderStatusResponseDto(order.getId(), order.getOrderStatus(), order.getUpdatedAt());
    }

    private OrderSummaryDto toSummary(OrderEntity order) {
        return new OrderSummaryDto(
            order.getId(),
            order.getOrderNumber(),
            order.getOrderStatus(),
            order.getPaymentStatus(),
            order.getTotalAmount(),
            order.getOrderedAt(),
            order.getCancelledAt()
        );
    }

    private OrderAddressDto toAddress(OrderAddressEntity address) {
        return new OrderAddressDto(address.getRecipientName(), address.getPhone(), address.getZipCode(), address.getAddress1(), address.getAddress2());
    }

    private OrderItemDto toItem(OrderItemEntity item) {
        return new OrderItemDto(
            item.getId(),
            item.getProduct().getId(),
            item.getProductOption() == null ? null : item.getProductOption().getId(),
            item.getProductNameSnapshot(),
            item.getOptionNameSnapshot(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineAmount()
        );
    }

    private PaymentResponseDto toPayment(PaymentEntity payment) {
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

    private BigDecimal unitPrice(CartItemEntity cartItem) {
        return cartItem.getProduct().getPrice().add(
            cartItem.getProductOption() == null ? BigDecimal.ZERO : cartItem.getProductOption().getAdditionalPrice()
        );
    }

    private String orderNumber() {
        return "ORD-" + LocalDate.now().toString().replace("-", "") + "-" + System.currentTimeMillis();
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    public record PagedOrders(List<OrderSummaryDto> items, PageMeta meta) {}
    public record PagedAdminOrders(List<AdminOrderSummaryDto> items, PageMeta meta) {}
}
