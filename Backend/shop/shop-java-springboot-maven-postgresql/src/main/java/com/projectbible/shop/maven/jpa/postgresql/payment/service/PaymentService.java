package com.projectbible.shop.maven.jpa.postgresql.payment.application;

import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final JdbcTemplate jdbcTemplate;

    public PaymentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> create(CurrentActor actor, Map<String, Object> body) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
        long orderId = number(body.get("orderId"), "orderId");
        String paymentMethod = required(body, "paymentMethod");
        List<Map<String, Object>> orderRows = jdbcTemplate.queryForList("select id, user_id as \"userId\", total_amount as \"totalAmount\" from orders where id = ?", orderId);
        if (orderRows.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Object> order = orderRows.get(0);
        if (((Number) order.get("userId")).longValue() != actor.id()) {
            throw new AppException("FORBIDDEN", "You cannot pay for this order", HttpStatus.FORBIDDEN);
        }
        if (!jdbcTemplate.queryForList("select id from payments where order_id = ?", orderId).isEmpty()) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Payment already exists", HttpStatus.CONFLICT);
        }
        Long paymentId = jdbcTemplate.queryForObject(
            "insert into payments (order_id, payment_method, payment_status, paid_amount, paid_at) values (?, ?, 'PAID', ?, now()) returning id",
            Long.class,
            orderId, paymentMethod, order.get("totalAmount")
        );
        jdbcTemplate.update("update orders set order_status = 'PAID', payment_status = 'PAID', updated_at = now() where id = ?", orderId);
        return one(paymentId == null ? -1 : paymentId, actor);
    }

    public Map<String, Object> one(long paymentId, CurrentActor actor) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            """
            select p.id, p.order_id as "orderId", p.payment_method as "paymentMethod", p.payment_status as "paymentStatus",
                   p.paid_amount as "paidAmount", p.paid_at as "paidAt", p.refunded_at as "refundedAt", o.user_id as "userId"
            from payments p
            join orders o on o.id = p.order_id
            where p.id = ?
            """,
            paymentId
        );
        if (rows.isEmpty()) {
            throw new AppException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Object> payment = rows.get(0);
        if (actor != null && actor.isUser() && ((Number) payment.get("userId")).longValue() != actor.id()) {
            throw new AppException("FORBIDDEN", "You cannot access this payment", HttpStatus.FORBIDDEN);
        }
        payment.remove("userId");
        return payment;
    }

    public Map<String, Object> refund(long paymentId) {
        one(paymentId, null);
        jdbcTemplate.update("update payments set payment_status = 'REFUNDED', refunded_at = now() where id = ?", paymentId);
        jdbcTemplate.update("update orders set payment_status = 'REFUNDED', order_status = 'CANCELLED', updated_at = now() where id = (select order_id from payments where id = ?)", paymentId);
        return one(paymentId, null);
    }

    private int number(Object value, String key) {
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (Exception ex) { throw new AppException("VALIDATION_ERROR", key + " must be a number", HttpStatus.BAD_REQUEST); }
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new AppException("VALIDATION_ERROR", key + " is required", HttpStatus.BAD_REQUEST);
        }
        return String.valueOf(value).trim();
    }
}
