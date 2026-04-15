package com.projectbible.shop.maven.jpa.postgresql.cart.application;

import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final JdbcTemplate jdbcTemplate;

    public CartService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list(CurrentActor actor) {
        requireUser(actor);
        return jdbcTemplate.queryForList(
            """
            select c.id, c.product_id as "productId", c.product_option_id as "productOptionId", c.quantity,
                   p.name as "productName", p.price,
                   po.name as "optionName", po.value as "optionValue", po.additional_price as "additionalPrice"
            from cart_items c
            join products p on p.id = c.product_id
            left join product_options po on po.id = c.product_option_id
            where c.user_id = ?
            order by c.id asc
            """,
            actor.id()
        );
    }

    public Map<String, Object> create(CurrentActor actor, Map<String, Object> body) {
        requireUser(actor);
        long productId = number(body.get("productId"), "productId");
        Long productOptionId = body.get("productOptionId") == null ? null : Long.valueOf(number(body.get("productOptionId"), "productOptionId"));
        int quantity = number(body.get("quantity"), "quantity");
        Integer count = jdbcTemplate.queryForObject("select count(*) from products where id = ? and status = 'ACTIVE'", Integer.class, productId);
        if (count == null || count == 0) {
            throw new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND);
        }
        if (productOptionId != null) {
            Integer optionCount = jdbcTemplate.queryForObject("select count(*) from product_options where id = ? and product_id = ?", Integer.class, productOptionId, productId);
            if (optionCount == null || optionCount == 0) {
                throw new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND);
            }
        }
        var existing = jdbcTemplate.queryForList("select id, quantity from cart_items where user_id = ? and product_id = ? and coalesce(product_option_id, 0) = coalesce(?, 0)", actor.id(), productId, productOptionId);
        if (!existing.isEmpty()) {
            long id = ((Number) existing.get(0).get("id")).longValue();
            int currentQty = ((Number) existing.get(0).get("quantity")).intValue();
            jdbcTemplate.update("update cart_items set quantity = ?, updated_at = now() where id = ?", currentQty + quantity, id);
            return one(actor, id);
        }
        Long id = jdbcTemplate.queryForObject(
            "insert into cart_items (user_id, product_id, product_option_id, quantity) values (?, ?, ?, ?) returning id",
            Long.class,
            actor.id(), productId, productOptionId, quantity
        );
        return one(actor, id == null ? -1 : id);
    }

    public Map<String, Object> update(CurrentActor actor, long cartItemId, Map<String, Object> body) {
        requireUser(actor);
        one(actor, cartItemId);
        int quantity = number(body.get("quantity"), "quantity");
        jdbcTemplate.update("update cart_items set quantity = ?, updated_at = now() where id = ? and user_id = ?", quantity, cartItemId, actor.id());
        return one(actor, cartItemId);
    }

    public Map<String, Object> remove(CurrentActor actor, long cartItemId) {
        requireUser(actor);
        one(actor, cartItemId);
        jdbcTemplate.update("delete from cart_items where id = ? and user_id = ?", cartItemId, actor.id());
        return Map.of("message", "Cart item deleted successfully");
    }

    public Map<String, Object> clear(CurrentActor actor) {
        requireUser(actor);
        jdbcTemplate.update("delete from cart_items where user_id = ?", actor.id());
        return Map.of("message", "Cart cleared successfully");
    }

    public Map<String, Object> one(CurrentActor actor, long cartItemId) {
        requireUser(actor);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            """
            select c.id, c.product_id as "productId", c.product_option_id as "productOptionId", c.quantity,
                   p.name as "productName", p.price,
                   po.name as "optionName", po.value as "optionValue", po.additional_price as "additionalPrice"
            from cart_items c
            join products p on p.id = c.product_id
            left join product_options po on po.id = c.product_option_id
            where c.id = ? and c.user_id = ?
            """,
            cartItemId, actor.id()
        );
        if (rows.isEmpty()) {
            throw new AppException("CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND);
        }
        return rows.get(0);
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    private int number(Object value, String key) {
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            throw new AppException("VALIDATION_ERROR", key + " must be a number", HttpStatus.BAD_REQUEST);
        }
    }
}
