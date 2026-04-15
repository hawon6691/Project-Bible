package com.projectbible.shop.maven.jpa.postgresql.order.application;

import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> create(CurrentActor actor, Map<String, Object> body) {
        requireUser(actor);
        if (!(body.get("cartItemIds") instanceof List<?> cartItemValues) || cartItemValues.isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "cartItemIds is required", HttpStatus.BAD_REQUEST);
        }
        List<Long> cartItemIds = cartItemValues.stream().map(v -> Long.valueOf(String.valueOf(v))).toList();
        long addressId = number(body.get("addressId"), "addressId");
        List<Map<String, Object>> addressRows = jdbcTemplate.queryForList(
            "select id, recipient_name as \"recipientName\", phone, zip_code as \"zipCode\", address1, address2 from addresses where id = ? and user_id = ?",
            addressId, actor.id()
        );
        if (addressRows.isEmpty()) {
            throw new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Object> address = addressRows.get(0);
        String placeholders = String.join(",", java.util.Collections.nCopies(cartItemIds.size(), "?"));
        List<Object> itemParams = new ArrayList<>();
        itemParams.add(actor.id());
        itemParams.addAll(cartItemIds);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            """
            select c.id, c.product_id as "productId", c.product_option_id as "productOptionId", c.quantity,
                   p.name as "productName", p.price, p.stock,
                   po.name as "optionName", po.value as "optionValue", po.additional_price as "additionalPrice", po.stock as "optionStock"
            from cart_items c
            join products p on p.id = c.product_id
            left join product_options po on po.id = c.product_option_id
            where c.user_id = ? and c.id in (""" + placeholders + ")",
            itemParams.toArray()
        );
        if (items.size() != cartItemIds.size()) {
            throw new AppException("CART_ITEM_NOT_FOUND", "Some cart items were not found", HttpStatus.NOT_FOUND);
        }
        int totalAmount = 0;
        for (Map<String, Object> item : items) {
            int available = item.get("productOptionId") != null ? ((Number) item.get("optionStock")).intValue() : ((Number) item.get("stock")).intValue();
            int quantity = ((Number) item.get("quantity")).intValue();
            if (quantity > available) {
                throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
            }
            int unitPrice = ((Number) item.get("price")).intValue() + (item.get("additionalPrice") == null ? 0 : ((Number) item.get("additionalPrice")).intValue());
            totalAmount += unitPrice * quantity;
        }
        String orderNumber = "ORD-" + java.time.LocalDate.now().toString().replace("-", "") + "-" + System.currentTimeMillis();
        Long orderId = jdbcTemplate.queryForObject(
            "insert into orders (user_id, order_number, order_status, total_amount, payment_status, ordered_at) values (?, ?, 'PENDING', ?, 'READY', now()) returning id",
            Long.class,
            actor.id(), orderNumber, totalAmount
        );
        jdbcTemplate.update(
            "insert into order_addresses (order_id, recipient_name, phone, zip_code, address1, address2) values (?, ?, ?, ?, ?, ?)",
            orderId, address.get("recipientName"), address.get("phone"), address.get("zipCode"), address.get("address1"), address.get("address2")
        );
        for (Map<String, Object> item : items) {
            int unitPrice = ((Number) item.get("price")).intValue() + (item.get("additionalPrice") == null ? 0 : ((Number) item.get("additionalPrice")).intValue());
            String optionSnapshot = item.get("productOptionId") == null ? null : String.valueOf(item.get("optionName")) + ":" + String.valueOf(item.get("optionValue"));
            jdbcTemplate.update(
                "insert into order_items (order_id, product_id, product_option_id, product_name_snapshot, option_name_snapshot, unit_price, quantity, line_amount) values (?, ?, ?, ?, ?, ?, ?, ?)",
                orderId, item.get("productId"), item.get("productOptionId"), item.get("productName"), optionSnapshot, unitPrice, item.get("quantity"), unitPrice * ((Number) item.get("quantity")).intValue()
            );
            if (item.get("productOptionId") != null) {
                jdbcTemplate.update("update product_options set stock = stock - ?, updated_at = now() where id = ?", item.get("quantity"), item.get("productOptionId"));
            }
            jdbcTemplate.update("update products set stock = stock - ?, updated_at = now() where id = ?", item.get("quantity"), item.get("productId"));
        }
        List<Object> deleteParams = new ArrayList<>();
        deleteParams.add(actor.id());
        deleteParams.addAll(cartItemIds);
        jdbcTemplate.update("delete from cart_items where user_id = ? and id in (" + placeholders + ")", deleteParams.toArray());
        return one(orderId == null ? -1 : orderId, actor);
    }

    public Map<String, Object> list(CurrentActor actor, Map<String, String> query) {
        requireUser(actor);
        int page = page(query.get("page"));
        int limit = limit(query.get("limit"));
        int offset = (page - 1) * limit;
        List<Object> params = new ArrayList<>();
        params.add(actor.id());
        String where = " where o.user_id = ?";
        if (query.get("status") != null && !query.get("status").isBlank()) {
            params.add(query.get("status").trim().toUpperCase());
            where += " and o.order_status = ?";
        }
        Integer totalCount = jdbcTemplate.queryForObject("select count(*) from orders o" + where, Integer.class, params.toArray());
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(limit);
        listParams.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            """
            select id, order_number as "orderNumber", order_status as "orderStatus", payment_status as "paymentStatus",
                   total_amount as "totalAmount", ordered_at as "orderedAt", cancelled_at as "cancelledAt"
            from orders o
            """ + where + """
            order by o.ordered_at desc
            limit ? offset ?
            """,
            listParams.toArray()
        );
        return Map.of("items", items, "meta", meta(page, limit, totalCount == null ? 0 : totalCount));
    }

    public Map<String, Object> one(long orderId, CurrentActor actor) {
        List<Object> params = new ArrayList<>();
        params.add(orderId);
        String where = " where o.id = ?";
        if (actor != null && actor.isUser()) {
            params.add(actor.id());
            where += " and o.user_id = ?";
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            """
            select o.id, o.order_number as "orderNumber", o.order_status as "orderStatus", o.payment_status as "paymentStatus",
                   o.total_amount as "totalAmount", o.ordered_at as "orderedAt", o.cancelled_at as "cancelledAt"
            from orders o
            """ + where,
            params.toArray()
        );
        if (rows.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND);
        }
        List<Map<String, Object>> orderAddress = jdbcTemplate.queryForList("select recipient_name as \"recipientName\", phone, zip_code as \"zipCode\", address1, address2 from order_addresses where order_id = ?", orderId);
        List<Map<String, Object>> orderItems = jdbcTemplate.queryForList(
            """
            select id, product_id as "productId", product_option_id as "productOptionId", product_name_snapshot as "productNameSnapshot",
                   option_name_snapshot as "optionNameSnapshot", unit_price as "unitPrice", quantity, line_amount as "lineAmount"
            from order_items
            where order_id = ?
            order by id asc
            """,
            orderId
        );
        List<Map<String, Object>> payment = jdbcTemplate.queryForList(
            """
            select id, order_id as "orderId", payment_method as "paymentMethod", payment_status as "paymentStatus",
                   paid_amount as "paidAmount", paid_at as "paidAt", refunded_at as "refundedAt"
            from payments where order_id = ?
            """,
            orderId
        );
        return Map.of("order", rows.get(0), "orderAddress", orderAddress.isEmpty() ? null : orderAddress.get(0), "orderItems", orderItems, "payment", payment.isEmpty() ? null : payment.get(0));
    }

    public Map<String, Object> cancel(CurrentActor actor, long orderId) {
        requireUser(actor);
        Map<String, Object> detail = one(orderId, actor);
        Map<String, Object> order = (Map<String, Object>) detail.get("order");
        String orderStatus = String.valueOf(order.get("orderStatus"));
        if (List.of("SHIPPING", "DELIVERED", "CANCELLED").contains(orderStatus)) {
            throw new AppException("PAYMENT_NOT_ALLOWED", "Order cannot be cancelled", HttpStatus.CONFLICT);
        }
        jdbcTemplate.update("update orders set order_status = 'CANCELLED', cancelled_at = now(), updated_at = now() where id = ?", orderId);
        return Map.of("id", orderId, "orderStatus", "CANCELLED", "cancelledAt", java.time.Instant.now().toString());
    }

    public Map<String, Object> adminList(Map<String, String> query) {
        int page = page(query.get("page"));
        int limit = limit(query.get("limit"));
        int offset = (page - 1) * limit;
        List<Object> params = new ArrayList<>();
        List<String> filters = new ArrayList<>(List.of("1=1"));
        if (query.get("search") != null && !query.get("search").isBlank()) {
            String pattern = "%" + query.get("search").trim() + "%";
            params.add(pattern);
            params.add(pattern);
            filters.add("(o.order_number ilike ? or u.name ilike ?)");
        }
        if (query.get("status") != null && !query.get("status").isBlank()) {
            params.add(query.get("status").trim().toUpperCase());
            filters.add("o.order_status = ?");
        }
        String where = " where " + String.join(" and ", filters);
        Integer totalCount = jdbcTemplate.queryForObject("select count(*) from orders o join users u on u.id = o.user_id" + where, Integer.class, params.toArray());
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(limit);
        listParams.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            """
            select o.id, o.order_number as "orderNumber", o.order_status as "orderStatus", o.payment_status as "paymentStatus",
                   o.total_amount as "totalAmount", o.ordered_at as "orderedAt",
                   json_build_object('id', u.id, 'name', u.name) as customer
            from orders o
            join users u on u.id = o.user_id
            """ + where + """
            order by o.ordered_at desc
            limit ? offset ?
            """,
            listParams.toArray()
        );
        return Map.of("items", items, "meta", meta(page, limit, totalCount == null ? 0 : totalCount));
    }

    public Map<String, Object> adminSetStatus(long orderId, String status) {
        String upper = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("PENDING", "PAID", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED").contains(upper)) {
            throw new AppException("INVALID_STATUS", "Invalid order status", HttpStatus.BAD_REQUEST);
        }
        one(orderId, null);
        return jdbcTemplate.queryForMap(
            "update orders set order_status = ?, updated_at = now() where id = ? returning id, order_status as \"orderStatus\", updated_at as \"updatedAt\"",
            upper, orderId
        );
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    private int page(String value) { return value == null || value.isBlank() ? 1 : Math.max(Integer.parseInt(value), 1); }
    private int limit(String value) { return value == null || value.isBlank() ? 20 : Math.max(Integer.parseInt(value), 1); }
    private int number(Object value, String key) {
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (Exception ex) { throw new AppException("VALIDATION_ERROR", key + " must be a number", HttpStatus.BAD_REQUEST); }
    }
    private Map<String, Object> meta(int page, int limit, int totalCount) {
        int totalPages = (int) Math.ceil(totalCount / (double) limit);
        return Map.of("page", page, "limit", limit, "totalCount", totalCount, "totalPages", totalPages);
    }
}
